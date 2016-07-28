/*
 * Copyright 2016 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.internal.participants.info;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.internal.Endpoints;
import com.samczsun.skype4j.internal.SkypeImpl;
import com.samczsun.skype4j.internal.Utils;
import com.samczsun.skype4j.participants.info.BotInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotInfoImpl implements BotInfo {
    private SkypeImpl skype;
    private String id;

    private String displayName;
    private String description;
    private String developer;
    private String extra;
    private String userTileSmallUrl;
    private String userTileMediumUrl;
    private String userTileLargeUrl;
    private String userTileExtraLargeUrl;
    private String userTileStaticUrl;
    private String webpage;
    private String tos;
    private String privacyStatement;
    private boolean isTrusted;
    private List<String> capabilities = new ArrayList<>();
    private List<String> supportedLocales = new ArrayList<>();
    private String agentType;
    private double starRating;

    public BotInfoImpl(SkypeImpl skype, String id) {
        this.skype = skype;
        this.id = id;
    }

    @Override
    public Skype getClient() {
        return this.skype;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getDeveloper() {
        return this.developer;
    }

    @Override
    public String getExtra() {
        return this.extra;
    }

    @Override
    public String getUserTileSmallUrl() {
        return this.userTileSmallUrl;
    }

    @Override
    public String getUserTileMediumUrl() {
        return this.userTileMediumUrl;
    }

    @Override
    public String getUserTileLargeUrl() {
        return this.userTileLargeUrl;
    }

    @Override
    public String getUserTileExtraLargeUrl() {
        return this.userTileExtraLargeUrl;
    }

    @Override
    public String getUserTileStaticUrl() {
        return this.userTileStaticUrl;
    }

    @Override
    public String getWebpage() {
        return this.webpage;
    }

    @Override
    public String getTos() {
        return this.tos;
    }

    @Override
    public String getPrivacyStatement() {
        return this.privacyStatement;
    }

    @Override
    public boolean isTrusted() {
        return this.isTrusted;
    }

    @Override
    public List<String> getCapabilities() {
        return Collections.unmodifiableList(this.capabilities);
    }

    @Override
    public List<String> getSupportedLocales() {
        return Collections.unmodifiableList(this.supportedLocales);
    }

    @Override
    public String getAgentType() {
        return this.agentType;
    }

    @Override
    public double getStarRating() {
        return this.starRating;
    }

    public void load() throws ConnectionException {
        String uuid = id.substring(3); // trims the string "28:"

        JsonObject root = Endpoints.AGENT_INFO.open(skype, uuid)
                .expect(200, "While fetching agent info")
                .as(JsonObject.class)
                .get();

        JsonArray descriptions = root.get("agentDescriptions").asArray();

        if (descriptions.size() > 1) {
            throw Skype.UNEXPECTED;
        }

        JsonObject object = descriptions.get(0).asObject();

        this.displayName = Utils.getString(object, "displayName");
        this.description = Utils.getString(object, "description");
        this.developer = Utils.getString(object, "developer");
        this.extra = Utils.getString(object, "extra");
        this.userTileSmallUrl = Utils.getString(object, "userTileSmallUrl");
        this.userTileMediumUrl = Utils.getString(object, "userTileMediumUrl");
        this.userTileLargeUrl = Utils.getString(object, "userTileLargeUrl");
        this.userTileExtraLargeUrl = Utils.getString(object, "userTileExtraLargeUrl");
        this.userTileStaticUrl = Utils.getString(object, "userTileStaticUrl");
        this.webpage = Utils.getString(object, "webpage");
        this.tos = Utils.getString(object, "tos");
        this.privacyStatement = Utils.getString(object, "privacyStatement");
        this.isTrusted = object.get("isTrusted").asBoolean();
        object.get("capabilities").asArray().iterator().forEachRemaining(value -> this.capabilities.add(value.asString()));
        object.get("supportedLocales").asArray().iterator().forEachRemaining(value -> this.supportedLocales.add(value.asString()));
        this.agentType = Utils.getString(object, "agentType");
        this.starRating = object.get("starRating").asDouble();
    }

}
