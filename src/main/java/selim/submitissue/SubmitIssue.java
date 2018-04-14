package selim.submitissue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SubmitIssue.MODID, name = SubmitIssue.NAME, version = SubmitIssue.VERSION,
		updateJSON = "http://myles-selim.us/modInfo/submitIssues.json", clientSideOnly = true)
public class SubmitIssue {

	public static final String MODID = "submitissue";
	public static final String NAME = "Submit Issue";
	public static final String VERSION = "1.0.1";
	@Mod.Instance(value = MODID)
	public static SubmitIssue instance;
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ClientCommandHandler.instance.registerCommand(new CommandSubmitIssue());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (ModConfig.VERBOSE) {
			ForgeVersion.CheckResult result = ForgeVersion
					.getResult(Loader.instance().activeModContainer());
			ForgeVersion.Status status = result.status;
			LOGGER.info(NAME + " is " + status);
			if (status == ForgeVersion.Status.OUTDATED || status == ForgeVersion.Status.BETA_OUTDATED)
				LOGGER.info("Please update to " + result.target + " before reporting any issues.");
		}
	}

}
