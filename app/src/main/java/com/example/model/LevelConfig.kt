package com.example.model

data class LevelConfig(
    val levelNumber: Int,
    val title: String,
    val description: String,
    val maxMoves: Int,
    val targetScore: Int,
    val starScores: Triple<Int, Int, Int>, // 1 star, 2 stars, 3 stars
    val targetJellies: Int = 0,
    val candyRequirements: Map<CandyColor, Int> = emptyMap(),
    val rows: Int = 8,
    val cols: Int = 8,
    val jellyMap: Set<Pair<Int, Int>> = emptySet()
) {
    companion object {
        fun getLevel(number: Int): LevelConfig {
            return levels.find { it.levelNumber == number } ?: levels.first()
        }

        val levels: List<LevelConfig> = listOf(
            LevelConfig(
                levelNumber = 1,
                title = "Sweet Start",
                description = "Match candies to reach 1,200 points!",
                maxMoves = 20,
                targetScore = 1200,
                starScores = Triple(1200, 2200, 3500)
            ),
            LevelConfig(
                levelNumber = 2,
                title = "Jelly Jam",
                description = "Clear 16 jelly tiles by matching over them!",
                maxMoves = 22,
                targetScore = 1800,
                starScores = Triple(1800, 3000, 4500),
                targetJellies = 16,
                jellyMap = buildSet {
                    for (r in 2..5) {
                        for (c in 2..5) {
                            add(r to c)
                        }
                    }
                }
            ),
            LevelConfig(
                levelNumber = 3,
                title = "Berry Crush",
                description = "Collect 20 Strawberry Hearts!",
                maxMoves = 18,
                targetScore = 2000,
                starScores = Triple(2000, 3500, 5000),
                candyRequirements = mapOf(CandyColor.RED to 20)
            ),
            LevelConfig(
                levelNumber = 4,
                title = "Blueberry Splash",
                description = "Collect 25 Blueberry Pops!",
                maxMoves = 20,
                targetScore = 2500,
                starScores = Triple(2500, 4200, 6000),
                candyRequirements = mapOf(CandyColor.BLUE to 25)
            ),
            LevelConfig(
                levelNumber = 5,
                title = "Sugar Carnival",
                description = "Reach 4,000 points with sweet combos!",
                maxMoves = 22,
                targetScore = 4000,
                starScores = Triple(4000, 6000, 8500)
            ),
            LevelConfig(
                levelNumber = 6,
                title = "Frosting Field",
                description = "Clear 24 jelly tiles across the board!",
                maxMoves = 24,
                targetScore = 3200,
                starScores = Triple(3200, 5200, 7500),
                targetJellies = 24,
                jellyMap = buildSet {
                    for (r in 1..6) {
                        for (c in 2..5) {
                            add(r to c)
                        }
                    }
                }
            ),
            LevelConfig(
                levelNumber = 7,
                title = "Citrus Delight",
                description = "Collect 20 Orange & 20 Lemon Candies!",
                maxMoves = 22,
                targetScore = 3500,
                starScores = Triple(3500, 5500, 8000),
                candyRequirements = mapOf(
                    CandyColor.ORANGE to 20,
                    CandyColor.YELLOW to 20
                )
            ),
            LevelConfig(
                levelNumber = 8,
                title = "Striped Sensation",
                description = "Trigger line blasts & score 5,500 points!",
                maxMoves = 22,
                targetScore = 5500,
                starScores = Triple(5500, 8000, 11000)
            ),
            LevelConfig(
                levelNumber = 9,
                title = "Cross Jelly Maze",
                description = "Clear 28 jelly tiles in the cross formation!",
                maxMoves = 25,
                targetScore = 4500,
                starScores = Triple(4500, 7000, 9500),
                targetJellies = 28,
                jellyMap = buildSet {
                    for (r in 0..7) {
                        add(r to 3)
                        add(r to 4)
                    }
                    for (c in 0..7) {
                        add(3 to c)
                        add(4 to c)
                    }
                }
            ),
            LevelConfig(
                levelNumber = 10,
                title = "Grape Royale",
                description = "Collect 30 Grape Jewels & reach 6,500 points!",
                maxMoves = 24,
                targetScore = 6500,
                starScores = Triple(6500, 9500, 13000),
                candyRequirements = mapOf(CandyColor.PURPLE to 30)
            ),
            LevelConfig(
                levelNumber = 11,
                title = "Candy Cascade",
                description = "Score 8,000 points with cascading chains!",
                maxMoves = 22,
                targetScore = 8000,
                starScores = Triple(8000, 11500, 15000)
            ),
            LevelConfig(
                levelNumber = 12,
                title = "Jelly Kingdom",
                description = "Clear all 36 central jelly tiles!",
                maxMoves = 26,
                targetScore = 6000,
                starScores = Triple(6000, 9000, 12500),
                targetJellies = 36,
                jellyMap = buildSet {
                    for (r in 1..6) {
                        for (c in 1..6) {
                            add(r to c)
                        }
                    }
                }
            ),
            LevelConfig(
                levelNumber = 13,
                title = "Mint & Berry Rush",
                description = "Collect 25 Mint Gumdrops and 25 Red Candies!",
                maxMoves = 24,
                targetScore = 7000,
                starScores = Triple(7000, 10500, 14000),
                candyRequirements = mapOf(
                    CandyColor.GREEN to 25,
                    CandyColor.RED to 25
                )
            ),
            LevelConfig(
                levelNumber = 14,
                title = "Grand Delicacy",
                description = "Reach 10,000 points in 25 moves!",
                maxMoves = 25,
                targetScore = 10000,
                starScores = Triple(10000, 14000, 18000)
            ),
            LevelConfig(
                levelNumber = 15,
                title = "Sugar Master Finale",
                description = "Clear 40 jellies and score 12,000 points!",
                maxMoves = 28,
                targetScore = 12000,
                starScores = Triple(12000, 17000, 22000),
                targetJellies = 40,
                jellyMap = buildSet {
                    for (r in 0..7) {
                        for (c in 0..7) {
                            if ((r + c) % 2 == 0) add(r to c)
                        }
                    }
                }
            )
        )
    }
}
