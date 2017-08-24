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

package com.facebook.stetho.inspector.elements.android;

import com.facebook.stetho.common.Accumulator;
import com.facebook.stetho.inspector.elements.AttributeAccumulator;
import com.facebook.stetho.inspector.elements.ComputedStyleAccumulator;
import com.facebook.stetho.inspector.elements.Descriptor;
import com.facebook.stetho.inspector.elements.NodeType;
import com.facebook.stetho.inspector.elements.StyleAccumulator;
import com.facebook.stetho.inspector.elements.StyleRuleNameAccumulator;

import javax.annotation.Nullable;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class RINoActivatedActivityDescriptor extends Descriptor<RINoActivatedActivityDescriptor> {

    @Override
    public void hook(RINoActivatedActivityDescriptor element) {

    }

    @Override
    public void unhook(RINoActivatedActivityDescriptor element) {

    }

    @Override
    public NodeType getNodeType(RINoActivatedActivityDescriptor element) {
        return NodeType.ELEMENT_NODE;
    }

    @Override
    public String getNodeName(RINoActivatedActivityDescriptor element) {
        return "no_activated_activity";
    }

    @Override
    public String getLocalName(RINoActivatedActivityDescriptor element) {
        return getNodeName(element);
    }

    @Nullable
    @Override
    public String getNodeValue(RINoActivatedActivityDescriptor element) {
        return null;
    }

    @Override
    public void getChildren(RINoActivatedActivityDescriptor element, Accumulator<Object> children) {

    }

    @Override
    public void getAttributes(RINoActivatedActivityDescriptor element, AttributeAccumulator attributes) {

    }

    @Override
    public void setAttributesAsText(RINoActivatedActivityDescriptor element, String text) {

    }

    @Override
    public void getStyleRuleNames(RINoActivatedActivityDescriptor element, StyleRuleNameAccumulator accumulator) {

    }

    @Override
    public void getStyles(RINoActivatedActivityDescriptor element, String ruleName, StyleAccumulator accumulator) {

    }

    @Override
    public void setStyle(RINoActivatedActivityDescriptor element, String ruleName, String name, String value) {

    }

    @Override
    public void getComputedStyles(RINoActivatedActivityDescriptor element, ComputedStyleAccumulator accumulator) {

    }
}
