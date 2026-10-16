package com.engineerinai.examples.football

import com.embabel.agent.domain.library.HasContent
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonClassDescription("A football team with its league")
data class Team(
    val name: String,
    @get:JsonPropertyDescription("League name, e.g. Premier League, La Liga, Bundesliga")
    val league: String,
)

@JsonClassDescription("Result of a football match")
data class MatchResult(
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    @get:JsonPropertyDescription("Date of the match, e.g. 2026-10-13")
    val date: String,
    @get:JsonPropertyDescription("Brief description of how the match played out")
    val summary: String,
)

@JsonClassDescription("Current league table position for a team")
data class LeaguePosition(
    val position: Int,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val points: Int,
    @get:JsonPropertyDescription("Goal difference")
    val goalDifference: Int,
)

@JsonClassDescription("Fan-friendly match analysis report")
data class FanReport(
    override val content: String,
) : HasContent
