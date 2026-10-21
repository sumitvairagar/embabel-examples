package com.engineerinai.examples.football

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.createObject
import com.embabel.agent.api.common.createObjectIfPossible
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto
import org.slf4j.LoggerFactory

@Agent(description = "Scout a football team: fetch their latest result and league position, then write a fan report")
class FootballScoutAgent(private val tavily: TavilySearchService) {

    private val logger = LoggerFactory.getLogger(FootballScoutAgent::class.java)

    @Action
    fun extractTeam(
        userInput: UserInput,
        context: OperationContext,
    ): Team? =
        context.ai().withDefaultLlm().createObjectIfPossible(
            """
            Extract the football team name and their league from this input.
            If no league is mentioned, infer it from the team name (e.g. Arsenal → Premier League).
            Input: ${userInput.content}
            """.trimIndent()
        )

    @Action
    fun fetchMatchResult(
        team: Team,
        context: OperationContext,
    ): MatchResult {
        val results = tavily.search("${team.name} match result score October 2026")
        val searchContext = results.joinToString("\n") { "- ${it.title}: ${it.content}" }
        logger.info("Tavily results for ${team.name} match:\n$searchContext")
        return context.ai().withDefaultLlm() createObject
            """
            Based ONLY on these search results, extract the most recent match result for ${team.name}.
            Do NOT invent or assume any details not explicitly stated in the results.
            If the score is not clearly stated, set homeScore and awayScore to 0.
            Return the home team, away team, score, date, and a one-sentence summary.

            Search results:
            $searchContext
            """.trimIndent()
    }

    @Action
    fun fetchLeaguePosition(
        team: Team,
        context: OperationContext,
    ): LeaguePosition {
        val results = tavily.search("${team.name} ${team.league} table standings October 2026")
        val searchContext = results.joinToString("\n") { "- ${it.title}: ${it.content}" }
        logger.info("Tavily results for ${team.name} league position:\n$searchContext")
        return context.ai().withDefaultLlm() createObject
            """
            Based ONLY on these search results, extract the current league table position for ${team.name}.
            Do NOT invent or assume any details not explicitly stated in the results.
            If a value is not clearly stated, use 0.
            Return position, games played, wins, draws, losses, points and goal difference.

            Search results:
            $searchContext
            """.trimIndent()
    }

    @AchievesGoal(description = "Write a fan-friendly report on a football team's recent form and league standing")
    @Action
    fun writeFanReport(
        team: Team,
        matchResult: MatchResult,
        leaguePosition: LeaguePosition,
        context: OperationContext,
    ): FanReport =
        context.ai().withLlm(LlmOptions(Auto).withTemperature(0.8)) createObject
            """
            You are a passionate but fair football analyst.
            Write a 150-word fan report for ${team.name} supporters covering:
            - Their most recent result: ${team.name} played on ${matchResult.date}.
              Score: ${matchResult.homeTeam} ${matchResult.homeScore} - ${matchResult.awayScore} ${matchResult.awayTeam}.
              ${matchResult.summary}
            - Their current league position: ${leaguePosition.position} in the ${team.league}
              with ${leaguePosition.points} points from ${leaguePosition.played} games
              (W${leaguePosition.won} D${leaguePosition.drawn} L${leaguePosition.lost}, GD ${leaguePosition.goalDifference}).
            Be direct, opinionated and end with a one-line verdict on current form.
            """.trimIndent()
}

