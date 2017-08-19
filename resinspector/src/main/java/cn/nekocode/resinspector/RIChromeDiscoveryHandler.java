/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package cn.nekocode.resinspector;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.facebook.stetho.common.ProcessUtil;
import com.facebook.stetho.inspector.ChromeDiscoveryHandler;
import com.facebook.stetho.server.SocketLike;
import com.facebook.stetho.server.http.HttpStatus;
import com.facebook.stetho.server.http.LightHttpBody;
import com.facebook.stetho.server.http.LightHttpRequest;
import com.facebook.stetho.server.http.LightHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Modified by nekocode (nekocode.cn@gmail.com)
 */
public class RIChromeDiscoveryHandler extends ChromeDiscoveryHandler {
    private static final String PAGE_ID = "1";
    private static final String PATH_PAGE_LIST = "/json";
    private static final String WEBKIT_REV = "@188492";

    private Context context;
    private String inspectorPath;

    public RIChromeDiscoveryHandler(Context context, String inspectorPath) {
        super(context, inspectorPath);
        this.context = context;
        this.inspectorPath = inspectorPath;
    }

    @Override
    public boolean handleRequest(SocketLike socket, LightHttpRequest request, LightHttpResponse response) {
        String path = request.uri.getPath();
        try {
            if (PATH_PAGE_LIST.equals(path)) {
                handlePageList(response);
                return true;
            }
        } catch (JSONException e) {
            response.code = HttpStatus.HTTP_INTERNAL_SERVER_ERROR;
            response.reasonPhrase = "Internal server error";
            response.body = LightHttpBody.create(e.toString() + "\n", "text/plain");
        }

        return super.handleRequest(socket, request, response);
    }

    private void handlePageList(LightHttpResponse response) throws JSONException {
        JSONArray reply = new JSONArray();
        JSONObject page = new JSONObject();
        page.put("type", "app");
        page.put("title", makeTitle());
        page.put("id", PAGE_ID);
        page.put("description", "");

        page.put("webSocketDebuggerUrl", "ws://" + inspectorPath);
        Uri chromeFrontendUrl = new Uri.Builder()
                .scheme("http")
                .authority("chrome-devtools-frontend.appspot.com")
                .appendEncodedPath("serve_rev")
                .appendEncodedPath(WEBKIT_REV)
                .appendEncodedPath("devtools.html")
                .appendQueryParameter("ws", inspectorPath)
                .build();
        page.put("devtoolsFrontendUrl", chromeFrontendUrl.toString());
        reply.put(page);

        response.code = HttpStatus.HTTP_OK;
        response.reasonPhrase = "OK";
        response.body = LightHttpBody.create(reply.toString(), "application/json");
    }

    private String makeTitle() {
        StringBuilder b = new StringBuilder();
        b.append(getAppLabel());
        b.append(" (powered by ResourceInspector)");

        String processName = ProcessUtil.getProcessName();
        int colonIndex = processName.indexOf(':');
        if (colonIndex >= 0) {
            String nonDefaultProcessName = processName.substring(colonIndex);
            b.append(nonDefaultProcessName);
        }

        return b.toString();
    }

    private CharSequence getAppLabel() {
        PackageManager pm = context.getPackageManager();
        return pm.getApplicationLabel(context.getApplicationInfo());
    }
}
