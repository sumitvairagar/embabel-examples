package com.engineerinai.examples.football

import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.createObject
import com.embabel.agent.api.common.createObjectIfPossible
import com.embabel.agent.core.CoreToolGroups
import com.embabel.agent.domain.io.UserInput
import com.embabel.common.ai.model.LlmOptions
import com.embabel.common.ai.model.ModelSelectionCriteria.Companion.Auto

@Agent(description = "Scout a football team: fetch their latest result and league position, then write a fan report")
class FootballScoutAgent {

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

    @Action(toolGroups = [CoreToolGroups.WEB])
    fun fetchMatchResult(
        team: Team,
        context: OperationContext,
    ): MatchResult =
        context.ai().withDefaultLlm() createObject
            """
            Use web search to find the most recent match result for ${team.name} in the ${team.league}.
            Return the home team, away team, score, date, and a one-sentence summary of how the match went.
            """.trimIndent()

    @Action(toolGroups = [CoreToolGroups.WEB])
    fun fetchLeaguePosition(
        team: Team,
        context: OperationContext,
    ): LeaguePosition =
        context.ai().withDefaultLlm() createObject
            """
            Use web search to find the current ${team.league} table position for ${team.name}.
            Return position, games played, wins, draws, losses, points and goal difference.
            """.trimIndent()

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
