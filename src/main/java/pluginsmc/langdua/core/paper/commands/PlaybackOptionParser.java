package pluginsmc.langdua.core.paper.commands;

import pluginsmc.langdua.core.paper.managers.PlayManager;

final class PlaybackOptionParser {
    record ParsedPlaybackOptions(PlayManager.PlaybackOptions playbackOptions, boolean forceWorldTeleport) {
    }

    private enum NormalizeMode {
        NONE(false, false),
        POSITION(true, false),
        ROTATION(false, true),
        BOTH(true, true);

        private final boolean position;
        private final boolean rotation;

        NormalizeMode(boolean position, boolean rotation) {
            this.position = position;
            this.rotation = rotation;
        }

        boolean position() {
            return position;
        }

        boolean rotation() {
            return rotation;
        }
    }

    private PlaybackOptionParser() {
    }

    static ParsedPlaybackOptions parse(String rawOptions, int defaultInterpolationSteps) {
        boolean bypassWorldMetadata = false;
        boolean forceWorldTeleport = false;
        int positionInterpolationSteps = defaultInterpolationSteps;
        int rotationInterpolationSteps = defaultInterpolationSteps;
        double smoothingPower = 1.0D;
        boolean releaseMode = false;
        int fps = 20;
        boolean dynamicFps = false;
        NormalizeMode normalizeMode = NormalizeMode.NONE;

        if (rawOptions != null && !rawOptions.isBlank()) {
            String[] tokens = rawOptions.trim().split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                if (token.equalsIgnoreCase("ignoreworld")) {
                    bypassWorldMetadata = true;
                    continue;
                }
                if (token.equals("--force")) {
                    forceWorldTeleport = true;
                    continue;
                }
                if (token.equals("--release") || token.equals("-r")) {
                    releaseMode = true;
                    continue;
                }
                if (token.startsWith("--normalize=")) {
                    normalizeMode = parseNormalizeMode(token.substring("--normalize=".length()));
                    continue;
                }
                if (token.equals("-n")) {
                    if (i + 1 >= tokens.length) {
                        throw new IllegalArgumentException("Missing value for -n");
                    }
                    normalizeMode = parseNormalizeMode(tokens[++i]);
                    continue;
                }
                if (token.startsWith("--posinterp=")) {
                    positionInterpolationSteps = parseUnsignedInt(token.substring("--posinterp=".length()), "posinterp");
                    continue;
                }
                if (token.equals("-p")) {
                    if (i + 1 >= tokens.length) {
                        throw new IllegalArgumentException("Missing value for -p");
                    }
                    positionInterpolationSteps = parseUnsignedInt(tokens[++i], "posinterp");
                    continue;
                }
                if (token.startsWith("--rotinterp=")) {
                    rotationInterpolationSteps = parseUnsignedInt(token.substring("--rotinterp=".length()), "rotinterp");
                    continue;
                }
                if (token.startsWith("--fps=")) {
                    fps = parseFps(token.substring("--fps=".length()));
                    continue;
                }
                if (token.equals("-f")) {
                    if (i + 1 >= tokens.length) {
                        throw new IllegalArgumentException("Missing value for -f");
                    }
                    fps = parseFps(tokens[++i]);
                    continue;
                }
                if (token.equals("--dfps") || token.equals("-d")) {
                    dynamicFps = true;
                    continue;
                }
                if (token.startsWith("--smoothpower=")) {
                    smoothingPower = parsePositiveDouble(token.substring("--smoothpower=".length()), "smoothpower");
                    continue;
                }
                if (token.startsWith("--smoothing=")) {
                    smoothingPower = parsePositiveDouble(token.substring("--smoothing=".length()), "smoothing");
                    continue;
                }
                if (token.equals("-s")) {
                    if (i + 1 >= tokens.length) {
                        throw new IllegalArgumentException("Missing value for -s");
                    }
                    smoothingPower = parsePositiveDouble(tokens[++i], "smoothpower");
                    continue;
                }
                throw new IllegalArgumentException("Unknown playback option: " + token);
            }
        }

        return new ParsedPlaybackOptions(
                new PlayManager.PlaybackOptions(
                        bypassWorldMetadata,
                        positionInterpolationSteps,
                        rotationInterpolationSteps,
                        smoothingPower,
                        normalizeMode.position(),
                        normalizeMode.rotation(),
                        releaseMode,
                        fps,
                        dynamicFps
                ),
                forceWorldTeleport
        );
    }

    private static int parseUnsignedInt(String value, String optionName) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new IllegalArgumentException("Option '" + optionName + "' must be at least 1");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for " + optionName + ": " + value, ex);
        }
    }

    private static double parsePositiveDouble(String value, String optionName) {
        try {
            double parsed = Double.parseDouble(value);
            if (parsed <= 0.0D) {
                throw new IllegalArgumentException("Option '" + optionName + "' must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number for " + optionName + ": " + value, ex);
        }
    }

    private static int parseFps(String value) {
        int parsed = parseUnsignedInt(value, "fps");
        if (parsed > 20) {
            return 20;
        }
        return parsed;
    }

    private static NormalizeMode parseNormalizeMode(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        return switch (normalized) {
            case "p" -> NormalizeMode.POSITION;
            case "r" -> NormalizeMode.ROTATION;
            case "pr", "rp" -> NormalizeMode.BOTH;
            default -> throw new IllegalArgumentException("Invalid normalize mode: " + value + " (use p, r, pr, or rp)");
        };
    }
}
