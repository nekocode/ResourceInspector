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

import com.facebook.stetho.inspector.ChromeDevtoolsServer;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.server.SocketLike;
import com.facebook.stetho.server.SocketLikeHandler;
import com.facebook.stetho.server.http.ExactPathMatcher;
import com.facebook.stetho.server.http.HandlerRegistry;
import com.facebook.stetho.server.http.LightHttpServer;
import com.facebook.stetho.websocket.WebSocketHandler;

import java.io.IOException;

/**
 * Modified by nekocode (nekocode.cn@gmail.com)
 */
public class RIDevtoolsSocketHandler implements SocketLikeHandler {
    private final Context mContext;
    private final Iterable<ChromeDevtoolsDomain> mModules;
    private final LightHttpServer mServer;

    public RIDevtoolsSocketHandler(Context context, Iterable<ChromeDevtoolsDomain> modules) {
        mContext = context;
        mModules = modules;
        mServer = createServer();
    }

    private LightHttpServer createServer() {
        HandlerRegistry registry = new HandlerRegistry();
        RIChromeDiscoveryHandler discoveryHandler =
                new RIChromeDiscoveryHandler(
                        mContext,
                        ChromeDevtoolsServer.PATH);
        discoveryHandler.register(registry);
        registry.register(
                new ExactPathMatcher(ChromeDevtoolsServer.PATH),
                new WebSocketHandler(new ChromeDevtoolsServer(mModules)));

        return new LightHttpServer(registry);
    }

    @Override
    public void onAccepted(SocketLike socket) throws IOException {
        mServer.serve(socket);
    }
}
