package com.stepstack.app.data

object SampleData {
    private const val BASE_URL = "https://images.unsplash.com/"

    val workouts: List<Workout> = listOf(
        // ---------------- CARDIO ----------------
        Workout(
            id = "c0",
            name = "Low-Impact Cardio Walk",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.BEGINNER,
            durationMin = 15,
            calories = 100,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Legs", "Heart"),
            description = "Gentle walking intervals with arm pumps. Perfect for getting started.",
            thumbnailUrl = "workout_running_prep",
            exercises = listOf(
                Exercise("c0e1", "March in Place", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 15, instructions = "March with high knees and rhythmic arm swings.", animation = ExerciseAnim.RUN),
                Exercise("c0e2", "Side Steps", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 15, instructions = "Step side to side while swinging arms horizontally.", animation = ExerciseAnim.GENERIC),
                Exercise("c0e3", "Arm Pumps", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 15, instructions = "Walk in place while pumping arms vigorously.", animation = ExerciseAnim.GENERIC),
                Exercise("c0e4", "Standing Knee Taps", ExerciseType.REPS, sets = 1, reps = 20, restSeconds = 15, instructions = "Lift knee to touch opposite hand.", animation = ExerciseAnim.RUN),
                Exercise("c0e5", "Toe Taps", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 15, instructions = "Tap toes forward alternating legs.", animation = ExerciseAnim.GENERIC),
                Exercise("c0e6", "Butt Kicks", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 15, instructions = "Kick heels towards glutes.", animation = ExerciseAnim.RUN),
                Exercise("c0e7", "Arm Circles", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 10, instructions = "Rotate arms in small circles.", animation = ExerciseAnim.GENERIC),
                Exercise("c0e8", "Half Squats", ExerciseType.REPS, sets = 1, reps = 15, restSeconds = 15, instructions = "Gentle squats, only halfway down.", animation = ExerciseAnim.SQUAT)
            )
        ),
        Workout(
            id = "c1",
            name = "Fat Burn HIIT Cardio",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 25,
            calories = 280,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "A no-equipment cardio session designed to spike your heart rate.",
            thumbnailUrl = "workout_fitness_class",
            exercises = listOf(
                Exercise("c1e1", "Jumping Jacks", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 15, instructions = "Jump feet apart while raising arms overhead.", animation = ExerciseAnim.JUMPING_JACKS),
                Exercise("c1e2", "High Knees", ExerciseType.TIME, sets = 1, durationSeconds = 40, restSeconds = 15, instructions = "Drive your knees up towards your chest at a fast pace.", animation = ExerciseAnim.RUN),
                Exercise("c1e3", "Burpees", ExerciseType.REPS, sets = 1, reps = 12, restSeconds = 30, instructions = "Squat, kick feet back, push-up, jump in, jump up.", animation = ExerciseAnim.PUSH_UP),
                Exercise("c1e4", "Mountain Climbers", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 15, instructions = "Rapidly alternate driving knees to chest in plank position.", animation = ExerciseAnim.PLANK),
                Exercise("c1e5", "Skaters", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 15, instructions = "Leap side to side, landing on one foot.", animation = ExerciseAnim.LUNGE),
                Exercise("c1e6", "Fast Feet", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 15, instructions = "Run in place with very small, fast steps.", animation = ExerciseAnim.RUN),
                Exercise("c1e7", "Plank Jacks", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 15, instructions = "In plank, jump feet out and in.", animation = ExerciseAnim.PLANK),
                Exercise("c1e8", "Box Shuffle", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 15, instructions = "Shuffle in a square pattern.", animation = ExerciseAnim.RUN)
            )
        ),
        Workout(
            id = "c4",
            name = "Sprint Interval Cardio",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.ADVANCED,
            durationMin = 25,
            calories = 400,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Legs", "Heart"),
            description = "Elite-level intervals. Maximum effort sprints and explosive movements.",
            thumbnailUrl = "workout_barbell",
            exercises = listOf(
                Exercise("c4e1", "Sprints in Place", ExerciseType.TIME, sets = 6, durationSeconds = 30, restSeconds = 10, instructions = "Run in place at maximum possible speed.", animation = ExerciseAnim.RUN),
                Exercise("c4e2", "Burpee Sprints", ExerciseType.TIME, sets = 4, durationSeconds = 45, restSeconds = 15, instructions = "Perform burpees immediately followed by 10s of sprinting.", animation = ExerciseAnim.PUSH_UP),
                Exercise("c4e3", "Tuck Jumps", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 20, instructions = "Jump high and bring knees to chest in mid-air.", animation = ExerciseAnim.SQUAT)
            )
        ),

        // ---------------- STRENGTH ----------------
        Workout(
            id = "s0",
            name = "Beginner Bodyweight Basics",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.BEGINNER,
            durationMin = 20,
            calories = 130,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Chest", "Legs", "Back"),
            description = "Build a solid foundation with these essential bodyweight movements.",
            thumbnailUrl = "workout_abs_core",
            exercises = listOf(
                Exercise("s0e1", "Wall Push-Ups", ExerciseType.REPS, sets = 1, reps = 12, restSeconds = 30, instructions = "Push away from a wall while keeping your body straight.", animation = ExerciseAnim.PUSH_UP),
                Exercise("s0e2", "Chair Squats", ExerciseType.REPS, sets = 1, reps = 12, restSeconds = 30, instructions = "Slowly sit down into a chair and stand back up.", animation = ExerciseAnim.SQUAT),
                Exercise("s0e3", "Glute Bridges", ExerciseType.REPS, sets = 1, reps = 15, restSeconds = 30, instructions = "Lie on back, lift hips towards ceiling.", animation = ExerciseAnim.CORE),
                Exercise("s0e4", "Bird-Dog", ExerciseType.REPS, sets = 1, reps = 10, restSeconds = 30, instructions = "On all fours, extend opposite arm and leg.", animation = ExerciseAnim.GENERIC),
                Exercise("s0e5", "Plank on Knees", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 30, instructions = "Hold a straight body position supported by knees and forearms.", animation = ExerciseAnim.PLANK),
                Exercise("s0e6", "Standing Calf Raises", ExerciseType.REPS, sets = 1, reps = 20, restSeconds = 20, instructions = "Rise up onto the balls of your feet.", animation = ExerciseAnim.GENERIC),
                Exercise("s0e7", "Incline Pushups", ExerciseType.REPS, sets = 1, reps = 10, restSeconds = 20, instructions = "Hands on an elevated surface.", animation = ExerciseAnim.PUSH_UP),
                Exercise("s0e8", "Lunges", ExerciseType.REPS, sets = 1, reps = 10, restSeconds = 20, instructions = "Step forward and lower hips.", animation = ExerciseAnim.LUNGE)
            )
        ),
        Workout(
            id = "s1",
            name = "Dumbbell Upper Body",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 35,
            calories = 250,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Arms", "Shoulders", "Chest"),
            description = "Build upper body strength with a focused dumbbell circuit.",
            thumbnailUrl = "workout_dumbbell_row",
            exercises = listOf(
                Exercise("s1e1", "Bicep Curls", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "Curl dumbbells toward shoulders.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e2", "Shoulder Press", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "Press dumbbells overhead.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e3", "Dumbbell Chest Press", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "Lie on back and press dumbbells upward.", animation = ExerciseAnim.PUSH_UP),
                Exercise("s1e4", "Lateral Raises", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "Lift dumbbells to the side.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e5", "Tricep Extensions", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "Extend weights overhead.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "s5",
            name = "Advanced Compound Circuit",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.ADVANCED,
            durationMin = 45,
            calories = 500,
            equipment = Equipment.FULL_GYM,
            targetMuscleGroups = listOf("Full Body", "Core"),
            description = "Heavy lifts and minimal rest for maximum strength and hypertrophy.",
            thumbnailUrl = "workout_gym_weights",
            exercises = listOf(
                Exercise("s5e1", "Front Squats", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 15, instructions = "Barbell across front deltoids, squat deep.", animation = ExerciseAnim.SQUAT),
                Exercise("s5e2", "Weighted Pull-Ups", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 15, instructions = "Pull chin over bar with added weight.", animation = ExerciseAnim.GENERIC),
                Exercise("s5e3", "Romanian Deadlifts", ExerciseType.REPS, sets = 4, reps = 10, restSeconds = 15, instructions = "Hinge at hips, lowering weight to mid-shin.", animation = ExerciseAnim.GENERIC),
                Exercise("s5e4", "Push Press", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 15, instructions = "Drive weight overhead using leg power.", animation = ExerciseAnim.GENERIC)
            )
        ),

        // ---------------- YOGA ----------------
        Workout(
            id = "y1",
            name = "Morning Yoga Flow",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.BEGINNER,
            durationMin = 15,
            calories = 60,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Flexibility"),
            description = "Gentle flow to wake up the body and mind.",
            thumbnailUrl = "workout_yoga_cobra",
            exercises = listOf(
                Exercise("y1e1", "Sun Salutation", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 15, instructions = "Controlled flow through the sun sequence.", animation = ExerciseAnim.GENERIC),
                Exercise("y1e2", "Cat-Cow", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 10, instructions = "Alternate arching and rounding back.", animation = ExerciseAnim.GENERIC),
                Exercise("y1e3", "Child's Pose", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 0, instructions = "Sit back on heels and relax.", animation = ExerciseAnim.BREATHING),
                Exercise("y1e4", "Downward Dog", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 10, instructions = "Hips high, heels towards floor.", animation = ExerciseAnim.GENERIC),
                Exercise("y1e5", "Cobra Pose", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 10, instructions = "Lift chest off floor.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "y4",
            name = "Vinyasa Flow",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 25,
            calories = 140,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Core", "Balance"),
            description = "Dynamic sequence linking breath to movement for strength and balance.",
            thumbnailUrl = "workout_yoga_advanced",
            exercises = listOf(
                Exercise("y4e1", "Warrior II Sequence", ExerciseType.TIME, sets = 2, durationSeconds = 90, restSeconds = 15, instructions = "Hold Warrior II, transition to Reverse Warrior.", animation = ExerciseAnim.LUNGE),
                Exercise("y4e2", "Side Angle Pose", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Extend side body while in lunge.", animation = ExerciseAnim.LUNGE),
                Exercise("y4e3", "Triangle Pose", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Deep lateral stretch and hip opener.", animation = ExerciseAnim.GENERIC),
                Exercise("y4e4", "Plank Flow", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 10, instructions = "Transition from plank to low plank.", animation = ExerciseAnim.PLANK),
                Exercise("y4e5", "Tree Pose", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 10, instructions = "Balance on one leg.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "y6",
            name = "Advanced Ashtanga Practice",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.ADVANCED,
            durationMin = 40,
            calories = 220,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Core", "Stability"),
            description = "High-intensity yoga practice focusing on power and advanced balances.",
            thumbnailUrl = "workout_yoga_lunge",
            exercises = listOf(
                Exercise("y6e1", "Headstand (Sirsasana)", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 30, instructions = "Practice balance in an inverted position.", animation = ExerciseAnim.CORE),
                Exercise("y6e2", "Crow to Plank Flow", ExerciseType.REPS, sets = 3, reps = 8, restSeconds = 10, instructions = "Transition smoothly from Crow pose to plank.", animation = ExerciseAnim.PLANK),
                Exercise("y6e3", "Deep Backbends", ExerciseType.REPS, sets = 3, reps = 5, restSeconds = 15, instructions = "Perform deep wheel or bridge poses.", animation = ExerciseAnim.CORE)
            )
        ),

        // ---------------- HIIT ----------------
        Workout(
            id = "h0",
            name = "HIIT Fundamentals",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.BEGINNER,
            durationMin = 12,
            calories = 120,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "Learn the basics of HIIT with modified moves and longer rest.",
            thumbnailUrl = "workout_crunches",
            exercises = listOf(
                Exercise("h0e1", "Modified Jumping Jacks", ExerciseType.TIME, sets = 2, durationSeconds = 40, restSeconds = 25, instructions = "Step out instead of jumping.", animation = ExerciseAnim.JUMPING_JACKS),
                Exercise("h0e2", "Step-Back Lunges", ExerciseType.TIME, sets = 2, durationSeconds = 40, restSeconds = 25, instructions = "Step back and lower your hips.", animation = ExerciseAnim.LUNGE),
                Exercise("h0e3", "Slow Mountain Climbers", ExerciseType.TIME, sets = 2, durationSeconds = 40, restSeconds = 25, instructions = "Focus on form and core engagement.", animation = ExerciseAnim.PLANK),
                Exercise("h0e4", "Standing Crunches", ExerciseType.REPS, sets = 2, reps = 15, restSeconds = 20, instructions = "Elbow to opposite knee.", animation = ExerciseAnim.CORE),
                Exercise("h0e5", "Shadow Boxing", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 20, instructions = "Punch the air in front of you.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "h1",
            name = "Tabata Blast",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 25,
            calories = 300,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Legs"),
            description = "Classic 20s-on/10s-off intervals for high intensity.",
            thumbnailUrl = "workout_trx_flyes",
            exercises = listOf(
                Exercise("h1e1", "Squat Jumps", ExerciseType.TIME, sets = 4, durationSeconds = 20, restSeconds = 10, instructions = "Explosive upward jumps.", animation = ExerciseAnim.SQUAT),
                Exercise("h1e2", "Push-Ups", ExerciseType.TIME, sets = 4, durationSeconds = 20, restSeconds = 10, instructions = "Fast-paced push-ups.", animation = ExerciseAnim.PUSH_UP),
                Exercise("h1e3", "Mountain Climbers", ExerciseType.TIME, sets = 4, durationSeconds = 20, restSeconds = 10, instructions = "Fast knees to chest in plank.", animation = ExerciseAnim.PLANK),
                Exercise("h1e4", "Burpees", ExerciseType.TIME, sets = 4, durationSeconds = 20, restSeconds = 10, instructions = "Chest to floor, jump up.", animation = ExerciseAnim.PUSH_UP),
                Exercise("h1e5", "High Knees", ExerciseType.TIME, sets = 4, durationSeconds = 20, restSeconds = 10, instructions = "Run in place with high knees.", animation = ExerciseAnim.RUN)
            )
        ),
        Workout(
            id = "h5",
            name = "Elite HIIT Circuit",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.ADVANCED,
            durationMin = 30,
            calories = 450,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "Explosive movements and weighted intervals for peak performance.",
            thumbnailUrl = "workout_deadlift",
            exercises = listOf(
                Exercise("h5e1", "Box Jumps (Simulated)", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 10, instructions = "Explosive vertical jumps.", animation = ExerciseAnim.SQUAT),
                Exercise("h5e2", "Kettlebell Swings (Simulated)", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 10, instructions = "Swing dumbbells or kettlebell explosively.", animation = ExerciseAnim.SQUAT),
                Exercise("h5e3", "Sprint Burpees", ExerciseType.REPS, sets = 4, reps = 12, restSeconds = 10, instructions = "Rapid burpees with high-speed finish.", animation = ExerciseAnim.PUSH_UP)
            )
        ),

        // ---------------- STRETCHING ----------------
        Workout(
            id = "st2",
            name = "Post-Workout Cooldown",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.BEGINNER,
            durationMin = 10,
            calories = 30,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body"),
            description = "Quick stretches to use after any session.",
            thumbnailUrl = "workout_meditation_beach",
            exercises = listOf(
                Exercise("st2e1", "Child's Pose", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 0, instructions = "Breathe deeply and relax.", animation = ExerciseAnim.BREATHING),
                Exercise("st2e2", "Cobra Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 10, instructions = "Stretch your abs and chest.", animation = ExerciseAnim.GENERIC),
                Exercise("st2e3", "Forward Fold", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 0, instructions = "Reach for toes and relax neck.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "st4",
            name = "Deep Stretch Flexibility",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 18,
            calories = 40,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Hips", "Hamstrings", "Back"),
            description = "Longer holds for improved range of motion.",
            thumbnailUrl = "workout_yoga_silhouette",
            exercises = listOf(
                Exercise("st4e1", "Pigeon Pose", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 15, instructions = "Deep hip opener.", animation = ExerciseAnim.GENERIC),
                Exercise("st4e2", "Splits Progression", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Gentle hamstring and hip flexor stretch.", animation = ExerciseAnim.LUNGE),
                Exercise("st4e3", "Deep Lunge Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Stretch hip flexors deeply.", animation = ExerciseAnim.LUNGE)
            )
        ),
        Workout(
            id = "st5",
            name = "Athlete Mobility Routine",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.ADVANCED,
            durationMin = 20,
            calories = 60,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Hips", "Shoulders"),
            description = "Active mobility and PNF stretching for high-performance recovery.",
            thumbnailUrl = "workout_yoga_sunset",
            exercises = listOf(
                Exercise("st5e1", "Loaded Stretches", ExerciseType.REPS, sets = 3, reps = 10, restSeconds = 10, instructions = "Use light weights to pull into deeper stretch ranges.", animation = ExerciseAnim.SQUAT),
                Exercise("st5e2", "PNF Stretching", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 10, instructions = "Contract and relax hamstring during stretch.", animation = ExerciseAnim.GENERIC),
                Exercise("st5e3", "Active Mobility Drills", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 10, instructions = "Side to side deep lunges and arm circles.", animation = ExerciseAnim.LUNGE)
            )
        )
    )

    val achievements: List<Achievement> = listOf(
        Achievement("a1", "First Steps", "Log your first day of activity", "footprint", false),
        Achievement("a2", "Week Warrior", "Hit your goal 5 days in a week", "calendar", false),
        Achievement("a3", "10K Club", "Walk 10,000 steps in a day", "steps", false),
        Achievement("a4", "Iron Streak", "Reach a 7-day streak", "flame", false),
        Achievement("a5", "Strong Start", "Complete 10 workouts", "dumbbell", false),
        Achievement("a6", "Century", "Log 100,000 lifetime steps", "trophy", false),
        Achievement("a7", "Hydro Hero", "Log 8 glasses of water in a day", "water_drop", false),
        Achievement("a8", "Explorer", "Complete a workout in every category", "explore", false)
    )

    val dailyTips: List<DailyTip> = listOf(
        DailyTip("Stay Hydrated", "Drinking water before a workout can improve your performance.", "water_drop"),
        DailyTip("Active Recovery", "On rest days, light walking or yoga can help reduce muscle soreness.", "self_improvement"),
        DailyTip("Protein Power", "Consuming protein after training helps repair muscle tissue.", "restaurant"),
        DailyTip("Sleep for Success", "Aim for 7-9 hours of sleep to fully recover.", "bedtime"),
        DailyTip("Consistency Wins", "A short workout done consistently is better than a long one done rarely.", "calendar_today"),
        DailyTip("Warm Up First", "Spend 5 minutes warming up to prevent injury.", "wb_sunny")
    )

    val masterExercises: List<Exercise> = workouts.flatMap { it.exercises }.distinctBy { it.name }

    val workoutPlans: List<WorkoutPlan> = listOf(
        WorkoutPlan(
            id = "p1",
            name = "7-Day Beginner Challenge",
            description = "Kickstart your fitness journey with light daily movement.",
            durationDays = 7,
            workoutsPerWeek = 7,
            schedule = mapOf(
                1 to "c0", // Low-Impact Cardio Walk
                2 to "st2", // Post-Workout Cooldown (used as light stretch)
                3 to "s0", // Beginner Bodyweight Basics
                4 to "m1", // Daily Mobility Flow
                5 to "h0", // HIIT Fundamentals
                6 to "y1", // Morning Yoga Flow
                7 to "st2"
            )
        ),
        WorkoutPlan(
            id = "p2",
            name = "4-Week Strength Program",
            description = "Build solid muscle and improve metabolic rate.",
            durationDays = 28,
            workoutsPerWeek = 4,
            schedule = mapOf(
                1 to "s1", // Dumbbell Upper Body
                3 to "s8", // Glute & Leg Sculpt
                5 to "s7", // Upper Body Power
                6 to "s5"  // Advanced Compound Circuit
            )
        )
    )

    fun getExerciseById(id: String): Exercise? = masterExercises.find { it.id == id }
}
