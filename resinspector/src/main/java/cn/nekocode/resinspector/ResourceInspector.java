/*
 * Copyright 2017. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.resinspector;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.stetho.common.ProcessUtil;
import com.facebook.stetho.inspector.elements.DescriptorProvider;
import com.facebook.stetho.inspector.elements.Document;
import com.facebook.stetho.inspector.elements.DocumentProviderFactory;
import com.facebook.stetho.inspector.elements.android.ActivityTracker;
import com.facebook.stetho.inspector.elements.android.RIAndroidDocumentProviderFactory;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.module.CSS;
import com.facebook.stetho.inspector.protocol.module.DOM;
import com.facebook.stetho.inspector.protocol.module.Page;
import com.facebook.stetho.server.LazySocketHandler;
import com.facebook.stetho.server.LocalSocketServer;
import com.facebook.stetho.server.ProtocolDetectingSocketHandler;
import com.facebook.stetho.server.ServerManager;
import com.facebook.stetho.server.SocketHandler;
import com.facebook.stetho.server.SocketHandlerFactory;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ResourceInspector {
    private static final String TAG = ResourceInspector.class.getSimpleName();
    private static final int TAG_RES_NAME = 0xF0F01358;
    private static final String DEVTOOLS_SUFFIX = "_devtools_remote";


    @NonNull
    public static Context wrap(@NonNull Context base) {
        return new InspectorContextWrapper(base);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static boolean isViewBeingInspected(@Nullable View view) {
        return view != null && view.getTag(ResourceInspector.TAG_RES_NAME) instanceof String;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Nullable
    public static String getViewLayoutResName(@Nullable View view) {
        return view != null ? (String) view.getTag(ResourceInspector.TAG_RES_NAME) : null;
    }

    public static void initialize(@NonNull final Context context) {
        ActivityTracker.get().beginTrackingIfPossible((Application) context.getApplicationContext());

        final SocketHandler socketHandler = new LazySocketHandler(
                new SocketHandlerFactory() {
                    @Override
                    public SocketHandler create() {
                        final ProtocolDetectingSocketHandler socketHandler =
                                new ProtocolDetectingSocketHandler(context);

                        final Iterable<ChromeDevtoolsDomain> inspectorModules = getInspectorModules(context);
                        if (inspectorModules != null) {
                            socketHandler.addHandler(
                                    new ProtocolDetectingSocketHandler.AlwaysMatchMatcher(),
                                    new RIDevtoolsSocketHandler(context, inspectorModules));
                        }

                        return socketHandler;
                    }
                }
        );

        final String className = ResourceInspector.class.getSimpleName();
        final LocalSocketServer server = new LocalSocketServer(
                className, className + ProcessUtil.getProcessName() + DEVTOOLS_SUFFIX, socketHandler);

        final ServerManager serverManager = new ServerManager(server);
        serverManager.start();
    }

    private static Iterable<ChromeDevtoolsDomain> getInspectorModules(Context context) {
        final ArrayList<ChromeDevtoolsDomain> modules = new ArrayList<>();

        final DocumentProviderFactory factory = new RIAndroidDocumentProviderFactory(
                (Application) context.getApplicationContext(), Collections.<DescriptorProvider>emptyList());
        final Document document = new Document(factory);
        modules.add(new DOM(document));
        modules.add(new CSS(document));
        modules.add(new Page(context));

        return modules;
    }

    /*
      Internal Classes
     */

    private static class InspectorContextWrapper extends ContextWrapper {
        private InspectorLayoutInflater inflater;

        InspectorContextWrapper(Context base) {
            super(base);
        }

        @Override
        public Object getSystemService(String name) {
            if (LAYOUT_INFLATER_SERVICE.equals(name)) {
                if (inflater == null) {
                    inflater = new InspectorLayoutInflater(
                            (LayoutInflater) super.getSystemService(name), this);
                }
                return inflater;
            }
            return super.getSystemService(name);
        }
    }

    private static class InspectorLayoutInflater extends LayoutInflater {
        private final LayoutInflater original;
        private final String appPackageName;

        InspectorLayoutInflater(LayoutInflater original, Context newContext) {
            super(original, newContext);
            this.original = original;
            appPackageName = getContext().getApplicationContext().getPackageName();
        }

        @Override
        public LayoutInflater cloneInContext(Context newContext) {
            return new InspectorLayoutInflater(this.original, newContext);
        }

        @Override
        public void setFactory(Factory factory) {
            original.setFactory(factory);
        }

        @Override
        public void setFactory2(Factory2 factory) {
            original.setFactory2(factory);
            setPrivateFactoryInternal();
        }

        @Override
        public View inflate(int resourceId, ViewGroup root, boolean attachToRoot) {
            final Resources res = getContext().getResources();
            final String packageName = res.getResourcePackageName(resourceId);
            final String resName = res.getResourceEntryName(resourceId);
            final View view = original.inflate(resourceId, root, attachToRoot);

            if (!appPackageName.equals(packageName)) {
                return view;
            }

            View targetView = view;
            if (root != null && attachToRoot) {
                targetView = root.getChildAt(root.getChildCount() - 1);
            }

            targetView.setTag(TAG_RES_NAME, resName);

            return view;
        }

        @Override
        public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
            return super.inflate(parser, root, attachToRoot);
        }

        private void setPrivateFactoryInternal() {
            // Skip if not attached to an activity.
            if (!(getContext() instanceof Factory2)) {
                return;
            }

            final Method setPrivateFactoryMethod = getMethod(LayoutInflater.class, "setPrivateFactory");
            if (setPrivateFactoryMethod != null) {
                invokeMethod(original, setPrivateFactoryMethod, (Factory2) getContext());
            }
        }

        static Method getMethod(Class clazz, String methodName) {
            final Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            return null;
        }

        static void invokeMethod(Object object, Method method, Object... args) {
            try {
                if (method == null) return;
                method.invoke(object, args);
            } catch (Exception e) {
                Log.d(TAG, "Can't invoke method using reflection", e);
            }
        }
    }
}
