/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.inspector.elements.android;

import android.app.Activity;

import com.facebook.stetho.common.Accumulator;
import com.facebook.stetho.inspector.elements.AbstractChainedDescriptor;
import com.facebook.stetho.inspector.elements.NodeType;

// For the root, we use 1 object for both element and descriptor.

/**
 * Modified by nekocode (nekocode.cn@gmail.com)
 */
final class RIAndroidDocumentRoot extends AbstractChainedDescriptor<RIAndroidDocumentRoot> {

  @Override
  protected NodeType onGetNodeType(RIAndroidDocumentRoot element) {
    return NodeType.DOCUMENT_NODE;
  }

  @Override
  protected String onGetNodeName(RIAndroidDocumentRoot element) {
    return "root";
  }

  @Override
  protected void onGetChildren(RIAndroidDocumentRoot element, Accumulator<Object> children) {
    final Activity topActivity = ActivityTracker.get().tryGetTopActivity();
    children.store(topActivity != null ? topActivity : new No_Activated_Activity());
  }

  private static class No_Activated_Activity {}
}
