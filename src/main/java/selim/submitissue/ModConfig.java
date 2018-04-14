package selim.submitissue;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SubmitIssue.MODID)
@Config.LangKey(SubmitIssue.MODID + ":config.title")
public class ModConfig {

	@Config.Comment({ "Should only set to true if requested to do so.", "This may spam your logs." })
	public static boolean VERBOSE = false;

	@Config.Comment({ "Issue tracker URLs.", "Overrides the URL set by the mod dev." })
	public static final Map<String, String> ISSUE_TRACKERS = new HashMap<>();

	static {
		// ISSUE_TRACKERS.put("modid", "issue_tracker");
	}

	// @Mod.EventBusSubscriber(modid = ModJamPacks.MODID, value = Side.CLIENT)
	public static class EventHandler {

		/**
		 * Inject the new values and save to the config file when the config has
		 * been changed from the GUI.
		 *
		 * @param event
		 *            The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(SubmitIssue.MODID)) {
				ConfigManager.sync(SubmitIssue.MODID, Config.Type.INSTANCE);
				changed = true;
			}
		}
	}

	private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean changed = true;
	private static HashMap<String, URI> uris = new HashMap<String, URI>();
	private static List<String> modIds = new LinkedList<String>();

	public static String[] getSupportedMods() {
		if (!changed)
			return modIds.toArray(new String[modIds.size()]);
		modIds = new LinkedList<String>();
		for (ModContainer mod : Loader.instance().getModList())
			if (getURI(mod) != null && !modIds.contains(mod.getModId()))
				modIds.add(mod.getModId());
		for (Entry<String, String> entry : ISSUE_TRACKERS.entrySet())
			if (isURIValid(entry.getValue()) && !modIds.contains(entry.getKey()))
				modIds.add(entry.getKey());
		return modIds.toArray(new String[modIds.size()]);
	}

	private static boolean isURIValid(String uri) {
		try {
			return new URI(uri) != null;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public static URI getURI(ModContainer mod) {
		if (mod == null)
			return null;
		return getURI(mod.getModId());
	}

	public static URI getURI(String modId) {
		if (!changed)
			return uris.get(modId);
		changed = false;
		uris = new HashMap<String, URI>();
		for (Entry<String, URI> entry : getFiles("submit_issue.json", new JsonParserURI()).entrySet()) {
			String id = entry.getKey();
			if (id == null || id.equals(""))
				continue;
			URI jsonUri = entry.getValue();
			URI configUri = null;
			try {
				String uri = ISSUE_TRACKERS.get(id);
				if (uri != null)
					configUri = new URI(ISSUE_TRACKERS.get(id));
			} catch (URISyntaxException e) {}
			if (configUri == null && jsonUri != null) {
				modIds.add(entry.getKey());
				uris.put(id, jsonUri);
			} else {
				modIds.add(entry.getKey());
				uris.put(id, configUri);
			}
		}
		return uris.get(modId);
	}

	private static <T> HashMap<String, T> getFiles(String path, IJsonParser<T> parser) {
		HashMap<String, T> vals = new HashMap<String, T>();
		ModContainer current = Loader.instance().activeModContainer();
		for (Entry<String, ModContainer> entry : Loader.instance().getIndexedModList().entrySet()) {
			ModContainer mod = entry.getValue();
			JsonContext ctx = new JsonContext(mod.getModId());

			CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/" + path, root -> {
				return true;
			}, (root, file) -> {
				Loader.instance().setActiveModContainer(mod);

				String relative = root.relativize(file).toString();
				if (!"json".equals(FilenameUtils.getExtension(file.toString()))
						|| relative.startsWith("_"))
					return true;

				String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
				ResourceLocation key = new ResourceLocation(ctx.getModId(), name);

				BufferedReader reader = null;
				try {
					reader = Files.newBufferedReader(file);
					JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
					T val = parser.parse(json, ctx);
					if (val != null)
						vals.put(mod.getModId(), val);
				} catch (JsonParseException e) {
					FMLLog.log.error("Parsing error loading recipe {}", key, e);
					return false;
				} catch (IOException e) {
					FMLLog.log.error("Couldn't read recipe {} from {}", key, file, e);
					return false;
				} finally {
					IOUtils.closeQuietly(reader);
				}
				return true;
			}, true, true);
		}
		Loader.instance().setActiveModContainer(current);
		return vals;
	}

}