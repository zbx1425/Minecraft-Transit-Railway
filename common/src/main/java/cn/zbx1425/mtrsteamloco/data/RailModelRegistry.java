package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import mtr.mappings.Text;
import mtr.mappings.Utilities;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RailModelRegistry {

    public static Map<String, RailModelProperties> elements = new HashMap<>();

    public static ModelCluster railNodeModel;

    public static void register(String key, RailModelProperties properties) {
        elements.put(key, properties);
    }

    public static void reload(ResourceManager resourceManager) {
        elements.clear();

        //
        register("", new RailModelProperties(Text.translatable("rail.mtrsteamloco.default"), null, 1f, 0f));
        // This is pulled from registry and shouldn't be shown
        register("null", new RailModelProperties(Text.translatable("rail.mtrsteamloco.hidden"), null, Float.MAX_VALUE, 0f));

        try {
            RawModel railNodeRawModel = MainClient.modelManager.loadRawModel(resourceManager,
                    ResourceLocation.parse("mtrsteamloco:models/rail_node.csv"), MainClient.atlasManager);
            railNodeModel = MainClient.modelManager.uploadVertArrays(railNodeRawModel);
        } catch (Exception ex) {
            Main.LOGGER.error("Failed loading rail node model", ex);
            MtrModelRegistryUtil.recordLoadingError("Failed loading Rail Node", ex);
        }

        List<Pair<ResourceLocation, Resource>> resources =
                MtrModelRegistryUtil.listResources(resourceManager, "mtrsteamloco", "rails", ".json");
        for (Pair<ResourceLocation, Resource> pair : resources) {
            try {
                try (InputStream is = Utilities.getInputStream(pair.getSecond())) {
                    JsonObject rootObj = (new JsonParser()).parse(IOUtils.toString(is, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (rootObj.has("model")) {
                        String key = FilenameUtils.getBaseName(pair.getFirst().getPath());
                        register(key, loadFromJson(resourceManager, key, rootObj));
                    } else {
                        for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            String key = entry.getKey().toLowerCase(Locale.ROOT);
                            register(key, loadFromJson(resourceManager, key, obj));
                        }
                    }
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Failed loading rail: " + pair.getFirst().toString(), ex);
                MtrModelRegistryUtil.recordLoadingError("Failed loading Rail " + pair.getFirst().toString(), ex);
            }
        }

        MainClient.railRenderDispatcher.clearRail();
    }

    private static final RailModelProperties EMPTY_PROPERTY = new RailModelProperties(
            Text.literal(""), null, 1f, 0
    );

    public static RailModelProperties getProperty(String key) {
        return elements.getOrDefault(key, EMPTY_PROPERTY);
    }

    private static RailModelProperties loadFromJson(ResourceManager resourceManager, String key, JsonObject obj) throws IOException {
        if (obj.has("atlasIndex")) {
            MainClient.atlasManager.load(
                    MtrModelRegistryUtil.resourceManager,  ResourceLocation.parse(obj.get("atlasIndex").getAsString())
            );
        }

        RawModel rawModel = MainClient.modelManager.loadRawModel(resourceManager,
                ResourceLocation.parse(obj.get("model").getAsString()), MainClient.atlasManager).copy();

        if (obj.has("textureId")) {
            rawModel.replaceTexture("default.png", ResourceLocation.parse(obj.get("textureId").getAsString()));
        }
        if (obj.has("flipV") && obj.get("flipV").getAsBoolean()) {
            rawModel.applyUVMirror(false, true);
        }

        if (obj.has("translation")) {
            JsonArray vec = obj.get("translation").getAsJsonArray();
            rawModel.applyTranslation(vec.get(0).getAsFloat(), vec.get(1).getAsFloat(), vec.get(2).getAsFloat());
        }
        if (obj.has("rotation")) {
            JsonArray vec = obj.get("rotation").getAsJsonArray();
            rawModel.applyRotation(new Vector3f(1, 0, 0), vec.get(0).getAsFloat());
            rawModel.applyRotation(new Vector3f(0, 1, 0), vec.get(1).getAsFloat());
            rawModel.applyRotation(new Vector3f(0, 0, 1), vec.get(2).getAsFloat());
        }
        if (obj.has("scale")) {
            JsonArray vec = obj.get("scale").getAsJsonArray();
            rawModel.applyScale(vec.get(0).getAsFloat(), vec.get(1).getAsFloat(), vec.get(2).getAsFloat());
        }
        if (obj.has("mirror")) {
            JsonArray vec = obj.get("mirror").getAsJsonArray();
            rawModel.applyMirror(
                    vec.get(0).getAsBoolean(), vec.get(1).getAsBoolean(), vec.get(2).getAsBoolean(),
                    vec.get(0).getAsBoolean(), vec.get(1).getAsBoolean(), vec.get(2).getAsBoolean()
            );
        }

        rawModel.sourceLocation = ResourceLocation.parse(rawModel.sourceLocation.toString() + "/" + key);

        float repeatInterval = obj.has("repeatInterval") ? obj.get("repeatInterval").getAsFloat() : 0.5f;
        float yOffset = obj.has("yOffset") ? obj.get("yOffset").getAsFloat() : 0f;

        return new RailModelProperties(Text.translatable(obj.get("name").getAsString()), rawModel, repeatInterval, yOffset);
    }
}
