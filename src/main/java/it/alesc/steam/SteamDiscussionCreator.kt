package it.alesc.steam

import com.sksamuel.hoplite.ConfigLoader
import it.alesc.steam.config.Config
import it.alesc.steam.config.Discussion
import it.alesc.steam.db.DBUtils.configureDatabase
import it.alesc.steam.db.DBUtils.postInsertedRecently
import it.alesc.steam.db.DBUtils.retrieveAppsToInsert
import it.alesc.steam.db.DBUtils.retrieveAppsToUpdate
import it.alesc.steam.db.DBUtils.trackInsertedPost
import it.alesc.steam.pages.LoginPage
import it.alesc.steam.pages.TradingForumPage
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.slf4j.LoggerFactory
import java.time.Duration

object SteamDiscussionCreator {
    private val logger = LoggerFactory.getLogger(SteamDiscussionCreator::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val executionConfiguration = ConfigLoader().loadConfigOrThrow<Config>("/config.yaml")
        configureDatabase(executionConfiguration.db)
        val appsToUpdate = retrieveAppsToUpdate(executionConfiguration.apps.map { it.toInt() })
        val appsToInsert = retrieveAppsToInsert(executionConfiguration.apps.map { it.toInt() })
        if (appsToUpdate.isEmpty() && appsToInsert.isEmpty()) {
            logger.info("No apps to update")
            return
        }

        val driver = configureDriver()
        logger.info("{} Apps to update", appsToUpdate.size + appsToInsert.size)
        performLogin(driver, executionConfiguration)
        createDiscussions(driver, appsToUpdate.union(appsToInsert).toList(), executionConfiguration.discussion)

        driver.quit()
    }

    private fun createDiscussions(driver: WebDriver, appsToUpdate: List<String>, discussion: Discussion) {
        val tradingForumPage = TradingForumPage(driver)
        var createdCount = 0

        appsToUpdate.forEach {
            val result = createDiscussionForApp(tradingForumPage, it, discussion)
            if (result == CreateResult.OK) {
                createdCount++
            }
            if (createdCount == 5 && appsToUpdate.size > 5) {
                logger.info("Created 5 discussions, quitting. Retry after a minute")
                return
            }
        }
    }

    private fun performLogin(driver: WebDriver, executionConfiguration: Config) {
        val loginPage = LoginPage(driver)
        loginPage.navigate()
        val (username, password) = executionConfiguration.credentials
        loginPage.insertCredentials(username, password)
        loginPage.login()
    }

    private fun createDiscussionForApp(tradingForumPage: TradingForumPage, appID: String, discussion: Discussion): CreateResult {
        tradingForumPage.navigate(appID)
        if (tradingForumPage.existsDiscussion { it.getAuthor() == discussion.author }) {
            logger.info("Discussion already exists for game \"{}\"", tradingForumPage.getAppName())
            return CreateResult.SKIP
        }
        if (postInsertedRecently(appID)) {
            logger.info("Post inserted recently for game \"{}\"", tradingForumPage.getAppName())
            return CreateResult.SKIP
        }
        if (!tradingForumPage.canStartDiscussion()) {
            logger.warn("Start discussion button not present. Skip game \"{}\"", tradingForumPage.getAppName())
            return CreateResult.SKIP
        }

        tradingForumPage.startDiscussion()
        tradingForumPage.typeTitle(discussion.title)
        tradingForumPage.typeText(discussion.text)
        val created = tradingForumPage.createDiscussion(discussion.simulation)
        if (created) {
            trackInsertedPost(appID, discussion.simulation)
            return CreateResult.OK
        }
        return CreateResult.KO
    }

    private fun configureDriver(): WebDriver {
        val driver: WebDriver = FirefoxDriver()
        driver.manage()
            .timeouts()
            .implicitlyWait(Duration.ofSeconds(5))
        return driver
    }

    enum class CreateResult {
        OK, KO, SKIP
    }
}