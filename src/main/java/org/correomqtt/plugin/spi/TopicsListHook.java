package org.correomqtt.plugin.spi;


import javafx.scene.layout.HBox;


public interface TopicsListHook extends BaseExtensionPoint {
    void onInstantiatePublishView(String connectionId, HBox hBox);
}
