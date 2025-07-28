package com.github.maharong.randombox.command;

import com.github.maharong.randombox.box.BoxManager;
import com.github.maharong.randombox.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * /randombox 명령어 처리 클래스.
 * 서브 명령어: list, give, reload
 */
public class RandomBoxCommand implements CommandExecutor, TabCompleter {

    private final BoxManager boxManager;

    public RandomBoxCommand(BoxManager boxManager) {
        this.boxManager = boxManager;
    }

    /**
     * 명령어 실행 처리
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            // 인수 없을 경우 도움말 출력
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> handleList(sender, args);
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            default -> MessageUtil.send(sender, "invalid.arguments");
        }
        return true;
    }

    /**
     * 도움말 메시지 출력
     */
    private void sendUsage(CommandSender sender) {
        for (String line : MessageUtil.getPlainList("command.usage")) {
            MessageUtil.sendRaw(sender, line);
        }
    }

    /**
     * /randombox list [id] 처리
     * - 인수 없음: 전체 박스 목록 출력
     * - id 있음: 해당 박스 정보 출력
     */
    private void handleList(CommandSender sender, String[] args) {
        if (args.length == 1) {
            boxManager.sendBoxList(sender);
        } else {
            boxManager.sendBoxInfo(sender, args[1]);
        }
    }

    /**
     * /randombox give <플레이어> <박스ID> [수량] 처리
     */
    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            MessageUtil.send(sender, "invalid.arguments");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            MessageUtil.send(sender, "box.not-found", "{id}", args[2]);
            return;
        }

        String boxId = args[2];
        int amount = 1;

        // 수량 파싱
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                MessageUtil.send(sender, "invalid.arguments");
                return;
            }
        }

        // 박스 지급 시도
        if (boxManager.giveBox(target, boxId, amount)) {
            MessageUtil.send(sender, "box.give", "{target}", target.getName(), "{box}", boxId);
            MessageUtil.send(target, "box.received", "{box}", boxId);
        } else {
            MessageUtil.send(sender, "box.not-found", "{id}", boxId);
        }
    }

    /**
     * /randombox reload 처리
     * box.yml과 message.yml을 다시 불러온다
     */
    private void handleReload(CommandSender sender) {
        boxManager.reload();
        MessageUtil.send(sender, "command.reload");
    }

    /**
     * 탭 완성 처리
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.hasPermission("randombox.admin")) return Collections.emptyList();

        // 첫 번째 인수: list, give, reload
        if (args.length == 1) {
            return partialMatch(args[0], List.of("list", "give", "reload"));
        }

        // 두 번째 인수: 플레이어 이름 (give)
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        // 박스 ID 탭 완성: list [id], give <player> <id>
        if ((args.length == 2 && args[0].equalsIgnoreCase("list"))
                || (args.length == 3 && args[0].equalsIgnoreCase("give"))) {
            return boxManager.getAllBoxIds().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }

    /**
     * 부분 일치하는 탭 완성 후보 필터링
     */
    private List<String> partialMatch(String input, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }
}
