package server;

import java.util.Collection;

public record ListGameResult(Collection<GameSummary> games) {
}
