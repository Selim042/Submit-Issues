package selim.submitissue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class CommandSubmitIssue implements ICommand {

	private final List<String> aliases;

	public CommandSubmitIssue() {
		aliases = new ArrayList<String>();
		aliases.add(this.getName());
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "submitissue";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/submitissue [modId]";
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		if (args.length != 1) {
			sender.sendMessage(new TextComponentTranslation("misc.submit_issues:failed_to_open"));
			return;
		}
		String modId = args[0];
		URI uri = ModConfig.getURI(modId);
		if (uri != null) {
			uriToOpen = uri;
			openWebLink(uri);
			// openUri(uri);
			ModContainer mod = Loader.instance().getIndexedModList().get(modId);
			sender.sendMessage(new TextComponentTranslation("misc.submit_issues:opening")
					.appendText(mod.getName()));
		} else
			sender.sendMessage(new TextComponentTranslation("misc.submit_issues:failed_to_open"));
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		if (args.length != 1)
			return null;
		List<String> results = new LinkedList<String>();
		for (String mod : ModConfig.getSupportedMods())
			if (mod.matches("(?i)" + args[0] + ".*"))
				results.add(mod);
		return results;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");
	private static URI uriToOpen;

	private void openUri(URI uri) {
		try {
			String s = uri.getScheme();
			if (s == null)
				throw new URISyntaxException(uri.toString(), "Missing protocol");
			if (!PROTOCOLS.contains(s.toLowerCase(Locale.ROOT)))
				throw new URISyntaxException(uri.toString(),
						"Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
			if (Minecraft.getMinecraft().gameSettings.chatLinksPrompt)
				Minecraft.getMinecraft().displayGuiScreen(new GuiConfirmOpenLink(new GuiYesNoCallback() {

					@Override
					public void confirmClicked(boolean result, int id) {
						if (id == 31102009) {
							if (result)
								openWebLink(uriToOpen);
							uriToOpen = null;
							// Minecraft.getMinecraft().displayGuiScreen(null);
						}
					}
				}, uri.toString(), 31102009, false));
			else
				this.openWebLink(uri);
		} catch (URISyntaxException urisyntaxexception) {
			SubmitIssue.LOGGER.error("Can't open url for {}", uri.toString(), urisyntaxexception);
		}
	}

	private void openWebLink(URI url) {
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop").invoke((Object) null);
			oclass.getMethod("browse", URI.class).invoke(object, url);
		} catch (Throwable throwable1) {
			Throwable throwable = throwable1.getCause();
			SubmitIssue.LOGGER.error("Couldn't open link: {}",
					(Object) (throwable == null ? "<UNKNOWN>" : throwable.getMessage()));
		}
	}

}
