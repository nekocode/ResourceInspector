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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class ResourceInspector {

    public static Context wrap(Context base) {
        return new ContextWrapper(base);
    }

    private static class ContextWrapper extends android.content.ContextWrapper {
        private InspectorLayoutInflater inflater;

        ContextWrapper(Context base) {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final Drawable foreground = targetView.getForeground();
                if (foreground == null) {
                    targetView.setForeground(new TipDrawable(resName));
                } else {
                    targetView.setForeground(new LayerDrawable(new Drawable[]{
                            foreground, new TipDrawable(resName)
                    }));
                }

            } else {
                final Drawable background = targetView.getBackground();
                if (background == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        targetView.setBackground(new TipDrawable(resName));
                    } else {
                        targetView.setBackgroundDrawable(new TipDrawable(resName));
                    }

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        targetView.setBackground(new LayerDrawable(new Drawable[] {
                                background, new TipDrawable(resName)
                        }));
                    } else {
                        targetView.setBackgroundDrawable(new LayerDrawable(new Drawable[] {
                                background, new TipDrawable(resName)
                        }));
                    }
                }
            }

            return view;
        }
    }

    private static class TipDrawable extends Drawable {
        private static final float TEXT_SIZE = sp2px(10);
        private static final float PADDING = dp2px(4);
        private static final float BOUNDARY_WIDTH = dp2px(2);
        private static final Paint paint = new TextPaint();
        private static final Path path = new Path();
        private static final Path path2 = new Path();
        private final String text;
        private final float textWidth, textHeight;
        private final float tcx, tcy;
        private int alpha = 200;

        static {
            paint.setAntiAlias(true);
            paint.setTextSize(TEXT_SIZE);
            paint.setTextAlign(Paint.Align.CENTER);
        }

        TipDrawable(String text) {
            this.text = text;
            final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            textWidth = paint.measureText(text) + PADDING * 2f;
            textHeight = TEXT_SIZE + PADDING * 2f;
            tcx = textWidth / 2f;
            tcy = textHeight / 2f - (fontMetrics.bottom + fontMetrics.top) / 2f;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final float left =0f, top = 0f, right = canvas.getWidth(), bottom = canvas.getHeight();

            /*
              Draw boundary and background
             */
            path.reset();
            path.moveTo(left, top);
            path.lineTo(right, top);
            path.lineTo(right, bottom);
            path.lineTo(left, bottom);
            path.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path2.reset();
                path2.moveTo(left + BOUNDARY_WIDTH, top + BOUNDARY_WIDTH);
                path2.lineTo(right - BOUNDARY_WIDTH, top + BOUNDARY_WIDTH);
                path2.lineTo(right - BOUNDARY_WIDTH, bottom - BOUNDARY_WIDTH);
                path2.lineTo(left + BOUNDARY_WIDTH, bottom - BOUNDARY_WIDTH);
                path2.close();
                path.op(path2, Path.Op.DIFFERENCE);

                path2.reset();
                path2.moveTo(left, top);
                path2.lineTo(left + textWidth, top);
                path2.lineTo(left + textWidth, top + textHeight);
                path2.lineTo(left, top + textHeight);
                path2.close();
                path.op(path2, Path.Op.UNION);

                paint.setColor(Color.BLACK);
                paint.setAlpha((int) (alpha * 0.5f));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);

            } else {
                paint.setColor(Color.BLACK);
                paint.setAlpha((int) (alpha * 0.5f));
                paint.setStrokeWidth(BOUNDARY_WIDTH * 2);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, paint);

                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(left, top, left + textWidth, top + textHeight, paint);
            }

            /*
              Draw text
             */
            paint.setColor(Color.WHITE);
            paint.setAlpha(alpha);
            canvas.drawText(text, left + tcx, top + tcy, paint);
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
            this.alpha = alpha;
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static float dp2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    private static float sp2px(float spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }
}
