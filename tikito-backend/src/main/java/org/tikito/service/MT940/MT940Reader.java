package org.tikito.service.MT940;

import lombok.extern.slf4j.Slf4j;
import org.tikito.service.MT940.lines.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class MT940Reader {
    public static List<MT940Block> read(final String data) {
        final List<String> lines = List.of(data.split("\n"));
        final List<MT940Block> blocks = new ArrayList<>();
        List<MT940Line> linesOfBlocks = new ArrayList<>();
        String bic = null;
        boolean foundSecondBic = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith(":")) {
                if (line.startsWith("-")) {
                    // block is finished
                    blocks.add(new MT940Block(linesOfBlocks));
                    linesOfBlocks = new ArrayList<>();
                    bic = null;
                    foundSecondBic = false;
                } else if (bic == null) {
                    bic = line;
                } else if (!foundSecondBic) {
                    foundSecondBic = true;
                } else if (!bic.equals(line)) {
                    log.error("Unexpected line {}", line);
                }
            } else {
                if (line.startsWith(":86:")) {
                    i++;
                    while (i < lines.size() && !lines.get(i).startsWith(":")) {
                        line = line + lines.get(i);
                        i++;
                    }
                    i--;
                }
                linesOfBlocks.add(parseMT490Line(line));
            }
        }

        if (!linesOfBlocks.isEmpty()) {
            blocks.add(new MT940Block(linesOfBlocks));
        }
        return blocks;
    }

    private static MT940Line parseMT490Line(final String line) {
        if (line.startsWith(":20:")) {
            return parseMT940Line20(line.substring(4));
        } else if (line.startsWith(":25:")) {
            return parseMT940Line25(line.substring(4));
        } else if (line.startsWith(":28:")) {
            return parseMT940Line28(line.substring(4));
        } else if (line.startsWith(":60F:")) {
            return parseMT940Line60F(line.substring(5));
        } else if (line.startsWith(":61:")) {
            return parseMT940Line61(line.substring(4));
        } else if (line.startsWith(":62F:")) {
            return parseMT940Line62F(line.substring(5));
        } else if (line.startsWith(":86:")) {
            return parseMT940Line86(line.substring(4));
        } else if (line.startsWith("BIC:")) {
            return parseMT940LineBIC(line.substring(4));
        } else if (line.startsWith("KENMERK:")) {
            return parseMT940LineKENMERK(line.substring(8));
        }

        return null;
    }

    private static MT940Line parseMT940Line20(final String line) {
        return new MT940Line20AccountReference(line);
    }

    private static MT940Line parseMT940Line25(final String line) {
        return new MT940Line25AccountIdentification(line);
    }

    private static MT940Line parseMT940Line28(final String line) {
        return new MT940Line28Statement(line);
    }

    private static MT940Line parseMT940Line60F(final String line) {
        return new MT940Line60OpeningBalance(line);
    }

    private static MT940Line parseMT940Line61(final String line) {
        return new MT940Line61Statement(line);
    }

    private static MT940Line parseMT940Line62F(final String line) {
        return new MT940Line62ClosingBalance(line);
    }

    private static MT940Line parseMT940Line86(final String line) {
        return new MT940Line86AccountInfo(line);
    }

    private static MT940Line parseMT940LineBIC(final String line) {
        return new MT940LineBIC(line);
    }

    private static MT940Line parseMT940LineKENMERK(final String line) {
        return new MT940LineKENMERK(line);
    }

}