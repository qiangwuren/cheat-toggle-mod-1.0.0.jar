package com.cheattoggle;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.lang.reflect.Field;

public class CheatToggleMod implements ModInitializer {
	public static volatile Boolean pendingOperatorItemsValue = null;
	private static Field settingsField;

	@Override
	public void onInitialize() {
		try {
			settingsField = PrimaryLevelData.class.getDeclaredField("settings");
			settingsField.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException("Failed to access PrimaryLevelData.settings", e);
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("cheattoggle")
				.then(Commands.literal("cheats")
					.then(Commands.argument("enable", BoolArgumentType.bool())
						.executes(context -> toggleCheats(context, BoolArgumentType.getBool(context, "enable")))))
				.then(Commands.literal("lockdifficulty")
					.then(Commands.argument("enable", BoolArgumentType.bool())
						.executes(context -> toggleDifficultyLock(context, BoolArgumentType.getBool(context, "enable")))))
				.then(Commands.literal("difficulty")
					.then(Commands.literal("peaceful")
						.executes(context -> setDifficulty(context, Difficulty.PEACEFUL)))
					.then(Commands.literal("easy")
						.executes(context -> setDifficulty(context, Difficulty.EASY)))
					.then(Commands.literal("normal")
						.executes(context -> setDifficulty(context, Difficulty.NORMAL)))
					.then(Commands.literal("hard")
						.executes(context -> setDifficulty(context, Difficulty.HARD))))
				.then(Commands.literal("operatoritems")
					.then(Commands.argument("enable", BoolArgumentType.bool())
						.executes(context -> toggleOperatorItems(context, BoolArgumentType.getBool(context, "enable")))))
			);
		});
	}

	private static int toggleOperatorItems(CommandContext<CommandSourceStack> context, boolean enable) {
		pendingOperatorItemsValue = enable;
		context.getSource().sendSuccess(
			() -> Component.literal("Operator items tab " + (enable ? "shown" : "hidden")), false);
		return 1;
	}

	private static void overrideHardcore(MinecraftServer server, boolean hardcore) {
		try {
			var worldData = (PrimaryLevelData) server.getWorldData();
			var current = (LevelSettings) settingsField.get(worldData);
			var newDiffSettings = new LevelSettings.DifficultySettings(
				current.difficultySettings().difficulty(),
				hardcore,
				current.difficultySettings().locked()
			);
			var newSettings = new LevelSettings(
				current.levelName(), current.gameType(), newDiffSettings,
				current.allowCommands(), current.dataConfiguration()
			);
			settingsField.set(worldData, newSettings);
		} catch (Exception ignored) {
		}
	}

	/**
	 * 26.3 的权限系统改为 {@code net.minecraft.server.permissions}：
	 * 玩家权限由 {@code MinecraftServer#getProfilePermissions} 计算，
	 * 单人游戏下直接读取世界数据里的 allowCommands 标志。
	 * 原 26.2 的 {@code PlayerList#setAllowCommandsForAllPlayers} 已被移除，
	 * 这里改为写世界数据 + 复刻集成服务端的权限刷新链路。
	 */
	private static int toggleCheats(CommandContext<CommandSourceStack> context, boolean enable) {
		var source = context.getSource();
		var server = source.getServer();

		if (!server.isSingleplayer()) {
			source.sendFailure(Component.literal("This command is only available in single player mode"));
			return 0;
		}

		// 写入世界数据；IntegratedServer#getCustomPermissionLevel 会实时读取它，
		// 因此无需重建服务端即可热生效。
		server.getWorldData().setAllowCommands(enable);

		// 复刻 IntegratedServer#setAllowCommands 的权限刷新链路。
		// 注：该类位于「客户端」源码集（Loom 的 splitEnvironmentSourceSets 把
		// Minecraft 拆成 common/client/server 三份），主源码集编译期不可见，
		// 故不直接调用；其内部的 updatePermissions() 正是下面的重发权限循环。
		refreshPermissions(server);
		server.saveAllChunks(true, false, true);

		source.sendSuccess(() -> Component.literal("Cheats have been " + (enable ? "enabled" : "disabled") + " for this world"), true);
		return 1;
	}

	/**
	 * 重新下发权限并刷新指令树，让热切换立即对已连接的玩家生效。
	 */
	private static void refreshPermissions(MinecraftServer server) {
		var playerList = server.getPlayerList();
		for (ServerPlayer player : playerList.getPlayers()) {
			playerList.sendPlayerPermissionLevel(player);
			server.getCommands().sendCommands(player);
		}
	}

	private static int toggleDifficultyLock(CommandContext<CommandSourceStack> context, boolean enable) {
		var source = context.getSource();
		var server = source.getServer();

		if (!server.isSingleplayer()) {
			source.sendFailure(Component.literal("This command is only available in single player mode"));
			return 0;
		}

		boolean wasHardcore = ((PrimaryLevelData) server.getWorldData()).isHardcore();
		if (wasHardcore) overrideHardcore(server, false);

		server.setDifficultyLocked(enable);

		if (wasHardcore) overrideHardcore(server, true);
		server.saveAllChunks(true, false, true);

		source.sendSuccess(() -> Component.literal("Difficulty lock has been " + (enable ? "enabled" : "disabled") + " for this world"), true);
		return 1;
	}

	private static int setDifficulty(CommandContext<CommandSourceStack> context, Difficulty difficulty) {
		var source = context.getSource();
		var server = source.getServer();

		if (!server.isSingleplayer()) {
			source.sendFailure(Component.literal("This command is only available in single player mode"));
			return 0;
		}

		boolean wasHardcore = ((PrimaryLevelData) server.getWorldData()).isHardcore();
		if (wasHardcore) overrideHardcore(server, false);

		server.setDifficulty(difficulty, true);

		if (wasHardcore) overrideHardcore(server, true);
		server.saveAllChunks(true, false, true);

		source.sendSuccess(() -> Component.literal("Difficulty set to " + difficulty.getDisplayName().getString()), true);
		return 1;
	}
}
