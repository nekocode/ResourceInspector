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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.View;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ResourceInspector {

    @NonNull
    public static Context wrap(@NonNull Context base) {
        return base;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static boolean isViewBeingInspected(@Nullable View view) {
        return false;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Nullable
    public static String getViewLayoutResName(@Nullable View view) {
        return null;
    }

    public static void initialize(@NonNull final Context context) {

    }
}
