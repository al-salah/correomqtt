package org.correomqtt.plugin.spi;


import com.arkea.asyncapi.v2.models.channels.Channel;
import javafx.scene.layout.HBox;

import java.util.Map;


public interface TopicsListHook extends BaseExtensionPoint {

    Map<String, Channel> getTopics();

    void onInstantiatePublishView(String connectionId, HBox hBox);
}
