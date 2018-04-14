package selim.submitissue;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.JsonContext;

public class JsonParserURI implements IJsonParser<URI> {

	@Override
	public URI parse(JsonObject json, JsonContext ctx) {
		JsonElement urlElement = json.get("issue_tracker");
		try {
			return new URI(urlElement.getAsString());
		} catch (URISyntaxException e) {
			return null;
		}
	}

	@Override
	public Class<URI> getType() {
		return URI.class;
	}

}
