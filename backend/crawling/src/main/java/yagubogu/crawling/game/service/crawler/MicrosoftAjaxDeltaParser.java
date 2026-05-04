package yagubogu.crawling.game.service.crawler;

import java.util.Optional;

final class MicrosoftAjaxDeltaParser {

    private MicrosoftAjaxDeltaParser() {
    }

    static Optional<String> findUpdatePanel(final String responseBody, final String panelId) {
        if (responseBody == null || panelId == null || panelId.isBlank()) {
            return Optional.empty();
        }

        int index = 0;
        while (index < responseBody.length()) {
            int lengthEnd = responseBody.indexOf('|', index);
            if (lengthEnd < 0) {
                return Optional.empty();
            }

            Integer contentLength = parseLength(responseBody.substring(index, lengthEnd));
            if (contentLength == null) {
                return Optional.empty();
            }

            int typeStart = lengthEnd + 1;
            int typeEnd = responseBody.indexOf('|', typeStart);
            int idStart = typeEnd + 1;
            int idEnd = typeEnd < 0 ? -1 : responseBody.indexOf('|', idStart);
            if (typeEnd < 0 || idEnd < 0) {
                return Optional.empty();
            }

            String type = responseBody.substring(typeStart, typeEnd);
            String id = responseBody.substring(idStart, idEnd);

            int contentStart = idEnd + 1;
            int contentEnd = contentStart + contentLength;
            if (contentEnd > responseBody.length()) {
                return Optional.empty();
            }

            String content = responseBody.substring(contentStart, contentEnd);
            if ("updatePanel".equals(type) && id.equals(panelId)) {
                return Optional.of(content);
            }

            index = contentEnd;
            if (index < responseBody.length() && responseBody.charAt(index) == '|') {
                index++;
            }
        }

        return Optional.empty();
    }

    private static Integer parseLength(final String rawLength) {
        try {
            return Integer.parseInt(rawLength);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
