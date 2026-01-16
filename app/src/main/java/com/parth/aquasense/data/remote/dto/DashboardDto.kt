package com.parth.aquasense.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Dashboard summary response from GET /tanks/dashboard
 */
@Serializable
data class DashboardDto(
    @SerialName("total_tanks")
    val totalTanks: Int,

    @SerialName("total_fish")
    val totalFish: Int,

    @SerialName("tanks_needing_attention")
    val tanksNeedingAttention: Int,

    @SerialName("recent_alerts")
    val recentAlerts: List<AlertDto>
)

/**
 * Alert item in dashboard
 */
@Serializable
data class AlertDto(
    @SerialName("tank_id")
    val tankId: String,

    @SerialName("tank_name")
    val tankName: String,

    @SerialName("type")
    val type: String,

    @SerialName("message")
    val message: String
)
