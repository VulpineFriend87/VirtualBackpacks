package top.vulpine.virtualBackpacks.util;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.regex.Pattern;

public class PermissionChecker {

    private static final String root = "virtualbackpacks";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    public static boolean hasPermission(CommandSender sender, String base, String... parts) {

        if (sender.hasPermission(root + ".admin") || sender.hasPermission(root + ".*")) {
            return true;
        }

        String basePermission = root + "." + base.toLowerCase();

        if (sender.hasPermission(basePermission) || sender.hasPermission(basePermission + ".*")) {
            return true;
        }

        if (parts == null) {
            return false;
        }

        for (String part : parts) {

            String subPermission = basePermission + "." + part.toLowerCase();

            if (sender.hasPermission(subPermission) || sender.hasPermission(subPermission + ".*")) {
                return true;
            }

        }

        return false;
    }

    public static Integer getDynamicPermission(CommandSender sender, String base, String... parts) {

        if (sender.hasPermission(root + ".admin") || sender.hasPermission(root + ".*")) {
            return -1;
        }

        StringBuilder basePermissionBuilder = new StringBuilder(root)
                .append(".")
                .append(base.toLowerCase());

        if (parts != null) {
            for (String part : parts) {
                if (part == null || part.isEmpty()) {
                    continue;
                }
                basePermissionBuilder.append(".").append(part.toLowerCase());
            }
        }

        String basePermission = basePermissionBuilder.toString();

        if (sender.hasPermission(basePermission) || sender.hasPermission(basePermission + ".*")) {
            return -1;
        }

        String prefix = basePermission + ".";
        Integer best = null;

        for (PermissionAttachmentInfo pai : sender.getEffectivePermissions()) {

            if (!pai.getValue()) {
                continue;
            }

            String perm = pai.getPermission();

            if (!perm.startsWith(prefix)) {
                continue;
            }

            String[] segments = perm.split("\\.");
            if (segments.length == 0) {
                continue;
            }

            String last = segments[segments.length - 1];

            if ("infinite".equalsIgnoreCase(last)) {
                return -1;
            }

            if (!NUMBER_PATTERN.matcher(last).matches()) {
                continue;
            }

            try {
                int val = Integer.parseInt(last);
                if (best == null || val > best) {
                    best = val;
                }
            } catch (NumberFormatException ignored) {
            }

        }

        return best;
    }

}
