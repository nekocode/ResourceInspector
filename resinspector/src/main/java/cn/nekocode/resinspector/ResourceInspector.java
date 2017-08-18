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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ResourceInspector {
    public static final int TAG_RES_NAME = 0xF0F01358;


    public static Context wrap(@NonNull Context base) {
        return new InspectorContextWrapper(base);
    }

    public static void initialize(@NonNull final Context context) {

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
        public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
            final Resources res = getContext().getResources();
            final String packageName = res.getResourcePackageName(resource);
            final String resName = res.getResourceEntryName(resource);
            final View view = original.inflate(resource, root, attachToRoot);

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
    }
}
