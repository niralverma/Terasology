// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui;

@FunctionalInterface
public interface BrowserHyperlinkListener {
    void hyperlinkClicked(String hyperlink);
}
