package yagubogu.crawling.game.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MicrosoftAjaxDeltaParserTest {

    @Test
    @DisplayName("findUpdatePanel - updatePanel fragment 추출")
    void findUpdatePanel_ExtractsFragment() {
        String panelId = "ctl00$ctl00$ctl00$cphContents$cphContents$cphContents$udpRecord";
        String fragment = "<div class=\"smsScore\">SSG | LG</div>";
        String response = fragment.length() + "|updatePanel|" + panelId + "|" + fragment
                + "|0|hiddenField|__VIEWSTATE||";

        var result = MicrosoftAjaxDeltaParser.findUpdatePanel(response, panelId);

        assertThat(result).contains(fragment);
    }

    @Test
    @DisplayName("findUpdatePanel - 대상 패널이 없으면 빈 Optional")
    void findUpdatePanel_ReturnsEmptyWhenPanelDoesNotExist() {
        String fragment = "<div class=\"smsScore\"></div>";
        String response = fragment.length() + "|updatePanel|anotherPanel|" + fragment + "|";

        var result = MicrosoftAjaxDeltaParser.findUpdatePanel(response, "targetPanel");

        assertThat(result).isEmpty();
    }
}
