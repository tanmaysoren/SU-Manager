package com.example.model

data class PresetLocation(
    val id: String,
    val name: String,
    val country: String,
    val flagEmoji: String,
    val latitude: Double,
    val longitude: Double,
    val defaultIp: String,
    val description: String
) {
    companion object {
        val PRESETS = listOf(
            PresetLocation(
                id = "tokyo",
                name = "Tokyo (Shibuya Crossing)",
                country = "Japan",
                flagEmoji = "🇯🇵",
                latitude = 35.6595,
                longitude = 139.7004,
                defaultIp = "203.0.113.195",
                description = "Famous intersection in Japan"
            ),
            PresetLocation(
                id = "paris",
                name = "Paris (Eiffel Tower)",
                country = "France",
                flagEmoji = "🇫🇷",
                latitude = 48.8584,
                longitude = 2.2945,
                defaultIp = "185.220.101.5",
                description = "Iconic monument in Western Europe"
            ),
            PresetLocation(
                id = "nyc",
                name = "New York (Times Square)",
                country = "United States",
                flagEmoji = "🇺🇸",
                latitude = 40.7580,
                longitude = -73.9855,
                defaultIp = "104.28.19.42",
                description = "Bustling commercial hub in US East"
            ),
            PresetLocation(
                id = "london",
                name = "London (Big Ben)",
                country = "United Kingdom",
                flagEmoji = "🇬🇧",
                latitude = 51.5007,
                longitude = -0.1246,
                defaultIp = "198.51.100.88",
                description = "Historic clock tower in London"
            ),
            PresetLocation(
                id = "sydney",
                name = "Sydney (Opera House)",
                country = "Australia",
                flagEmoji = "🇦🇺",
                latitude = -33.8568,
                longitude = 151.2153,
                defaultIp = "139.130.4.5",
                description = "Performing arts center on Sydney Harbour"
            ),
            PresetLocation(
                id = "zurich",
                name = "Zurich (City Center)",
                country = "Switzerland",
                flagEmoji = "🇨🇭",
                latitude = 47.3769,
                longitude = 8.5417,
                defaultIp = "194.230.79.11",
                description = "Financial hub in central Europe"
            ),
            PresetLocation(
                id = "sf",
                name = "San Francisco (Golden Gate)",
                country = "United States",
                flagEmoji = "🇺🇸",
                latitude = 37.8199,
                longitude = -122.4783,
                defaultIp = "192.0.2.101",
                description = "Pacific coast landmark in Silicon Valley region"
            ),
            PresetLocation(
                id = "dubai",
                name = "Dubai (Burj Khalifa)",
                country = "United Arab Emirates",
                flagEmoji = "🇦🇪",
                latitude = 25.1972,
                longitude = 55.2744,
                defaultIp = "5.36.21.14",
                description = "Tallest building in Middle East"
            )
        )
    }
}

data class PresetIpNode(
    val ip: String,
    val country: String,
    val city: String,
    val flagEmoji: String,
    val latencyMs: Int,
    val provider: String,
    val carrier: String = "Cloudflare Inc.",
    val asn: String = "AS13335"
) {
    companion object {
        val NODES = listOf(
            PresetIpNode("104.28.19.42", "United States", "New York", "🇺🇸", 24, "CloudNet US-East", "Cloudflare Inc.", "AS13335"),
            PresetIpNode("185.220.101.5", "France", "Paris", "🇫🇷", 38, "EuSecure Node 12", "Orange S.A.", "AS3215"),
            PresetIpNode("203.0.113.195", "Japan", "Tokyo", "🇯🇵", 112, "AsiaPac Relay JP", "NTT Communications", "AS2914"),
            PresetIpNode("198.51.100.88", "United Kingdom", "London", "🇬🇧", 45, "UK CyberTunnel", "British Telecom", "AS2856"),
            PresetIpNode("139.130.4.5", "Australia", "Sydney", "🇦🇺", 165, "AussieShield Edge", "Telstra Corp", "AS1221"),
            PresetIpNode("194.230.79.11", "Switzerland", "Zurich", "🇨🇭", 30, "Alpine Secure Node", "Swisscom AG", "AS3303"),
            PresetIpNode("5.36.21.14", "UAE", "Dubai", "🇦🇪", 140, "Gulf Gate Proxy", "du Telecom", "AS15802")
        )
    }
}
