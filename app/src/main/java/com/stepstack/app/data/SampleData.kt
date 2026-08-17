package com.stepstack.app.data

object SampleData {
    private const val BASE_URL = "https://images.unsplash.com/"

    val workouts: List<Workout> = listOf(
        // ---------------- CARDIO ----------------
        Workout(
            id = "c0",
            name = "Gentle Start Cardio Walk",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.BEGINNER,
            durationMin = 15,
            calories = 95,
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
            name = "Cardio Burn Circuit",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 25,
            calories = 270,
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
            calories = 350,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Legs", "Heart"),
            description = "High-effort intervals mixing sprints, jumps and explosive footwork.",
            thumbnailUrl = "workout_barbell",
            exercises = listOf(
                Exercise("c4e1", "Sprints in Place", ExerciseType.TIME, sets = 6, durationSeconds = 30, restSeconds = 10, instructions = "Run in place at maximum possible speed.", easierModification = "Drop to a fast march.", harderModification = "Add a resistance band around your ankles.", targetMuscle = "Legs", animation = ExerciseAnim.RUN),
                Exercise("c4e2", "Burpee Sprints", ExerciseType.TIME, sets = 4, durationSeconds = 45, restSeconds = 15, instructions = "Perform burpees immediately followed by 10s of sprinting.", easierModification = "Step back into the burpee instead of jumping.", harderModification = "Add a push-up at the bottom of each burpee.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP),
                Exercise("c4e3", "Tuck Jumps", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 20, instructions = "Jump high and bring knees to chest in mid-air.", easierModification = "Perform standard squat jumps instead.", harderModification = "Add a full 180-degree turn each jump.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("c4e4", "Lateral Bounds", ExerciseType.REPS, sets = 4, reps = 16, restSeconds = 15, instructions = "Explosively push off one leg and land on the other, side to side.", easierModification = "Step side to side instead of jumping.", harderModification = "Hold the landing for 2 seconds before the next bound.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("c4e5", "Sprint Finisher", ExerciseType.TIME, sets = 3, durationSeconds = 40, restSeconds = 15, instructions = "All-out sprint in place to close out the circuit.", easierModification = "Sustain a strong jog pace.", harderModification = "Pump arms as hard and fast as possible.", targetMuscle = "Heart", animation = ExerciseAnim.RUN)
            )
        ),
        Workout(
            id = "c5",
            name = "Elite Cardio Engine",
            category = WorkoutCategory.CARDIO,
            difficulty = Difficulty.EXPERT,
            durationMin = 25,
            calories = 460,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Heart", "Legs", "Full Body"),
            description = "Pro-level cardio session designed for maximum VO2 max improvement.",
            thumbnailUrl = "workout_fitness_class",
            exercises = listOf(
                Exercise("c5e1", "100m Dash (Simulated)", ExerciseType.TIME, sets = 5, durationSeconds = 30, restSeconds = 15, instructions = "Max effort sprint in place with high knees and arm pumps.", easierModification = "Standard high knees.", harderModification = "Add resistance bands if available.", targetMuscle = "Heart", animation = ExerciseAnim.RUN),
                Exercise("c5e2", "Speed Skaters", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "Leap side-to-side with maximal distance and speed.", easierModification = "Slower, smaller steps.", harderModification = "Touch the floor on every rep.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("c5e3", "Burpee Broad Jumps", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 15, instructions = "Perform a burpee, then immediately jump as far forward as possible.", easierModification = "Step-back burpees into step-forward.", harderModification = "Jump back to starting point.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP),
                Exercise("c5e4", "Mountain Climber Sprints", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "Fastest possible mountain climbers in a perfect plank.", easierModification = "Slow, controlled climbers.", harderModification = "Add a push-up every 10 reps.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("c5e5", "Triple Jump Jacks", ExerciseType.REPS, sets = 5, reps = 25, restSeconds = 15, instructions = "Three small hops followed by one massive jumping jack.", easierModification = "Standard jumping jacks.", harderModification = "Add a squat at the bottom of the jack.", targetMuscle = "Full Body", animation = ExerciseAnim.JUMPING_JACKS)
            )
        ),

        // ---------------- STRENGTH ----------------
        Workout(
            id = "s0",
            name = "Foundation Strength Basics",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.BEGINNER,
            durationMin = 20,
            calories = 120,
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
                Exercise("s0e7", "Incline Push-Ups", ExerciseType.REPS, sets = 1, reps = 10, restSeconds = 20, instructions = "Hands on an elevated surface.", animation = ExerciseAnim.PUSH_UP),
                Exercise("s0e8", "Lunges", ExerciseType.REPS, sets = 1, reps = 10, restSeconds = 20, instructions = "Step forward and lower hips.", animation = ExerciseAnim.LUNGE)
            )
        ),
        Workout(
            id = "s1",
            name = "Upper Body Dumbbell Builder",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 35,
            calories = 300,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Arms", "Shoulders", "Chest"),
            description = "Build upper body strength with a focused dumbbell circuit.",
            thumbnailUrl = "workout_dumbbell_row",
            exercises = listOf(
                Exercise("s1e1", "Bicep Curls", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 30, instructions = "Curl dumbbells toward shoulders, keeping elbows pinned to your sides.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e2", "Shoulder Press", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 30, instructions = "Press dumbbells overhead without arching your lower back.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e3", "Dumbbell Chest Press", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 30, instructions = "Lie on back and press dumbbells upward over your chest.", animation = ExerciseAnim.PUSH_UP),
                Exercise("s1e4", "Lateral Raises", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 30, instructions = "Lift dumbbells out to the side to shoulder height.", animation = ExerciseAnim.GENERIC),
                Exercise("s1e5", "Tricep Extensions", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 30, instructions = "Extend a dumbbell overhead, bending only at the elbow.", animation = ExerciseAnim.GENERIC)
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
                Exercise("s5e1", "Front Squats", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 60, instructions = "Barbell across front deltoids, squat deep.", animation = ExerciseAnim.SQUAT),
                Exercise("s5e2", "Weighted Pull-Ups", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 60, instructions = "Pull chin over bar with added weight.", animation = ExerciseAnim.GENERIC),
                Exercise("s5e3", "Romanian Deadlifts", ExerciseType.REPS, sets = 4, reps = 10, restSeconds = 60, instructions = "Hinge at hips, lowering weight to mid-shin.", animation = ExerciseAnim.GENERIC),
                Exercise("s5e4", "Push Press", ExerciseType.REPS, sets = 4, reps = 8, restSeconds = 60, instructions = "Drive weight overhead using leg power.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "s7",
            name = "Explosive Upper Body Power",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.ADVANCED,
            durationMin = 35,
            calories = 400,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Chest", "Back", "Shoulders"),
            description = "Advanced resistance training focusing on upper body explosive power.",
            thumbnailUrl = "workout_dumbbell_row",
            exercises = listOf(
                Exercise("s7e1", "Wide-Grip Push-Ups", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 20, instructions = "Push-ups with hands wider than shoulders to target chest.", easierModification = "Perform on knees.", harderModification = "Elevate feet on a chair.", targetMuscle = "Chest", animation = ExerciseAnim.PUSH_UP),
                Exercise("s7e2", "Renegade Rows", ExerciseType.REPS, sets = 4, reps = 16, restSeconds = 20, instructions = "In plank, row dumbbells toward hips while keeping core stable.", easierModification = "Perform without weights.", harderModification = "Add a push-up between rows.", targetMuscle = "Back", animation = ExerciseAnim.PLANK),
                Exercise("s7e3", "Arnold Press", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 20, instructions = "Shoulder press with palms rotating from facing you to facing away.", easierModification = "Standard shoulder press.", harderModification = "Perform while seated without back support.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("s7e4", "Diamond Push-Ups", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 20, instructions = "Hands together in diamond shape to focus on triceps.", easierModification = "Perform on knees.", harderModification = "Pause at the bottom for 2 seconds.", targetMuscle = "Arms", animation = ExerciseAnim.PUSH_UP),
                Exercise("s7e5", "Superman Lat Pulls", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 20, instructions = "Lying face down, lift chest and pull elbows back as if doing a pull-up.", easierModification = "Keep feet on the floor.", harderModification = "Hold a light resistance band.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "s8",
            name = "Lower Body & Glute Sculpt",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.ADVANCED,
            durationMin = 40,
            calories = 450,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Legs", "Glutes"),
            description = "Intense leg day designed to build strength and definition.",
            thumbnailUrl = "workout_gym_weights",
            exercises = listOf(
                Exercise("s8e1", "Bulgarian Split Squats", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 20, instructions = "One foot elevated behind you, squat down on the front leg.", easierModification = "Standard lunges on the floor.", harderModification = "Hold dumbbells in both hands.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("s8e2", "Weighted Glute Bridges", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 20, instructions = "Lie on back, lift hips with a dumbbell resting on your lap.", easierModification = "Bodyweight glute bridges.", harderModification = "Single-leg glute bridges.", targetMuscle = "Glutes", animation = ExerciseAnim.CORE),
                Exercise("s8e3", "Goblet Squats", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 20, instructions = "Hold a dumbbell at your chest and squat deep.", easierModification = "Bodyweight squats.", harderModification = "Add a 3-second pause at the bottom.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("s8e4", "Walking Lunges", ExerciseType.REPS, sets = 4, reps = 30, restSeconds = 20, instructions = "Take large steps forward, lowering hips between each step.", easierModification = "Alternating lunges in place.", harderModification = "Hold dumbbells at your sides.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("s8e5", "Curtsy Lunges", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 20, instructions = "Step one leg back and across the other, as if curtsying.", easierModification = "Side lunges.", harderModification = "Add a hop when returning to center.", targetMuscle = "Glutes", animation = ExerciseAnim.LUNGE)
            )
        ),
        Workout(
            id = "s9",
            name = "Iron Will Full Body",
            category = WorkoutCategory.STRENGTH,
            difficulty = Difficulty.EXPERT,
            durationMin = 30,
            calories = 450,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Chest", "Core"),
            description = "High-rep, expert-level bodyweight strength circuit.",
            thumbnailUrl = "workout_abs_core",
            exercises = listOf(
                Exercise("s9e1", "Archer Push-Ups", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 15, instructions = "Push-ups while sliding one arm out straight to the side.", easierModification = "Side-to-side push-ups with bent arms.", harderModification = "One-arm push-ups.", targetMuscle = "Chest", animation = ExerciseAnim.PUSH_UP),
                Exercise("s9e2", "Pistol Squats", ExerciseType.REPS, sets = 5, reps = 16, restSeconds = 15, instructions = "One-legged squat with the other leg extended forward.", easierModification = "Hold a wall or TRX strap for balance.", harderModification = "Hold a light weight in front of you.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("s9e3", "Supermans", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "Lying face down, lift arms and legs off the floor and hold.", easierModification = "Lift only arms or only legs.", harderModification = "Flutter arms and legs like swimming.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC),
                Exercise("s9e4", "Plank Up-Downs", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "Move from forearm plank to hand plank and back.", easierModification = "Hold a static plank.", harderModification = "Add a push-up in the high plank position.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("s9e5", "Hollow Body Rock", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "Lie on back, lift arms and legs, and rock back and forth.", easierModification = "Keep knees bent.", harderModification = "Keep arms and legs as low as possible.", targetMuscle = "Abs", animation = ExerciseAnim.CORE)
            )
        ),

        // ---------------- YOGA ----------------
        Workout(
            id = "y1",
            name = "Morning Yoga Flow",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.BEGINNER,
            durationMin = 15,
            calories = 65,
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
            name = "Vinyasa Balance Flow",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 25,
            calories = 165,
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
            calories = 300,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Core", "Stability"),
            description = "High-intensity yoga practice focusing on power and advanced balances.",
            thumbnailUrl = "workout_yoga_lunge",
            exercises = listOf(
                Exercise("y6e1", "Sun Salutation B", ExerciseType.TIME, sets = 3, durationSeconds = 90, restSeconds = 15, instructions = "Move through the full traditional sequence with controlled breath.", easierModification = "Slow the pace and skip jump-throughs.", harderModification = "Add a vinyasa between every pose.", targetMuscle = "Full Body", animation = ExerciseAnim.GENERIC),
                Exercise("y6e2", "Headstand (Sirsasana)", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 30, instructions = "Practice balance in an inverted position against a wall.", easierModification = "Practice tripod headstand prep against a wall.", harderModification = "Hold away from the wall.", targetMuscle = "Core", animation = ExerciseAnim.CORE),
                Exercise("y6e3", "Crow to Plank Flow", ExerciseType.REPS, sets = 3, reps = 8, restSeconds = 10, instructions = "Transition smoothly from Crow pose to plank.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("y6e4", "Deep Backbends", ExerciseType.REPS, sets = 3, reps = 5, restSeconds = 15, instructions = "Perform deep wheel or bridge poses.", targetMuscle = "Back", animation = ExerciseAnim.CORE),
                Exercise("y6e5", "Seated Forward Fold Sequence", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 15, instructions = "Long, controlled holds to close out the practice and lengthen the spine.", easierModification = "Bend knees generously.", harderModification = "Deepen the fold with each exhale.", targetMuscle = "Hamstrings", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "y7",
            name = "Elite Power Yoga Flow",
            category = WorkoutCategory.YOGA,
            difficulty = Difficulty.EXPERT,
            durationMin = 30,
            calories = 300,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Core", "Balance"),
            description = "A demanding sequence of advanced yoga asanas and transitions.",
            thumbnailUrl = "workout_yoga_advanced",
            exercises = listOf(
                Exercise("y7e1", "Handstand Kickups", ExerciseType.REPS, sets = 5, reps = 10, restSeconds = 15, instructions = "Practice kicking up into a handstand against a wall or in open space.", easierModification = "Practice L-stands with feet on a wall.", harderModification = "Hold the handstand for 10 seconds.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("y7e2", "Eight-Angle Pose", ExerciseType.TIME, sets = 5, durationSeconds = 45, restSeconds = 15, instructions = "An advanced arm balance with legs hooked over one arm.", easierModification = "Elephant's trunk pose.", harderModification = "Transition into Grasshopper pose.", targetMuscle = "Arms", animation = ExerciseAnim.CORE),
                Exercise("y7e3", "Peacock Pose", ExerciseType.TIME, sets = 5, durationSeconds = 30, restSeconds = 15, instructions = "Balance your body on your elbows with your hands facing back.", easierModification = "Kneeling peacock pose.", harderModification = "Peacock with legs in Lotus position.", targetMuscle = "Core", animation = ExerciseAnim.CORE),
                Exercise("y7e4", "Wild Thing", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 15, instructions = "From downward dog, flip over into a one-armed backbend.", easierModification = "Side plank.", harderModification = "Transition into a full Wheel pose.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC),
                Exercise("y7e5", "Wheel Pose Push-Ups", ExerciseType.REPS, sets = 5, reps = 12, restSeconds = 15, instructions = "In a full backbend, lower your head toward the floor and push back up.", easierModification = "Static Wheel pose hold.", harderModification = "Lift one leg while doing the push-up.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC)
            )
        ),

        // ---------------- HIIT ----------------
        Workout(
            id = "h0",
            name = "HIIT Fundamentals",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.BEGINNER,
            durationMin = 12,
            calories = 100,
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
            calories = 290,
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
            name = "Elite Dumbbell HIIT Circuit",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.ADVANCED,
            durationMin = 30,
            calories = 440,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "Explosive movements and weighted intervals for peak performance.",
            thumbnailUrl = "workout_deadlift",
            exercises = listOf(
                Exercise("h5e1", "Dumbbell Squat Jumps", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 15, instructions = "Hold light dumbbells at your sides and jump explosively out of each squat.", easierModification = "Drop the dumbbells and use bodyweight only.", harderModification = "Increase the weight or add a pause at the bottom.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("h5e2", "Dumbbell Swings", ExerciseType.REPS, sets = 4, reps = 20, restSeconds = 15, instructions = "Hinge at the hips and swing a single dumbbell explosively to shoulder height.", easierModification = "Use a lighter weight and a smaller range of motion.", harderModification = "Swing overhead like an American kettlebell swing.", targetMuscle = "Full Body", animation = ExerciseAnim.SQUAT),
                Exercise("h5e3", "Sprint Burpees", ExerciseType.REPS, sets = 4, reps = 12, restSeconds = 15, instructions = "Rapid burpees finishing with a 5-second sprint in place.", easierModification = "Skip the sprint finish.", harderModification = "Add a push-up at the bottom of each rep.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP),
                Exercise("h5e4", "Dumbbell Thrusters", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 15, instructions = "Squat with dumbbells at your shoulders, then drive them overhead as you stand.", easierModification = "Separate the squat and press into two movements.", harderModification = "Increase tempo and reduce rest.", targetMuscle = "Full Body", animation = ExerciseAnim.SQUAT)
            )
        ),
        Workout(
            id = "h6",
            name = "Maximum Overdrive",
            category = WorkoutCategory.HIIT,
            difficulty = Difficulty.EXPERT,
            durationMin = 20,
            calories = 420,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "The most intense HIIT session in the app. No mercy, all results.",
            thumbnailUrl = "workout_deadlift",
            exercises = listOf(
                Exercise("h6e1", "Sprawl to Box Jump", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 10, instructions = "Drop to a sprawl (plank), jump up, and perform a max height vertical jump.", easierModification = "Sprawl to a simple stand-up.", harderModification = "Add a tuck jump instead of a vertical jump.", targetMuscle = "Full Body", animation = ExerciseAnim.SQUAT),
                Exercise("h6e2", "Explosive Push-Ups", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 10, instructions = "Push up so hard your hands leave the floor.", easierModification = "Standard push-ups.", harderModification = "Touch your chest mid-air.", targetMuscle = "Chest", animation = ExerciseAnim.PUSH_UP),
                Exercise("h6e3", "Navy Seal Burpees", ExerciseType.REPS, sets = 5, reps = 15, restSeconds = 10, instructions = "Burpee with three push-ups and two knee-drives at the bottom.", easierModification = "Standard burpees.", harderModification = "Add a pull-up if you have a bar.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP),
                Exercise("h6e4", "Squat Thrusts", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "In plank, jump feet to hands and back at a blistering pace.", easierModification = "Step feet in and out.", harderModification = "Add a push-up every 5 reps.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("h6e5", "Simulated Battle Rope Slams", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "Perform explosive slamming motions with your arms in a half-squat.", easierModification = "Slower arm movements.", harderModification = "Add a jump on every slam.", targetMuscle = "Arms", animation = ExerciseAnim.GENERIC)
            )
        ),

        // ---------------- STRETCHING ----------------
        Workout(
            id = "st2",
            name = "Post-Workout Cooldown",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.BEGINNER,
            durationMin = 10,
            calories = 35,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body"),
            description = "Quick stretches to use after any session.",
            thumbnailUrl = "workout_meditation_beach",
            exercises = listOf(
                Exercise("st2e1", "Child's Pose", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 0, instructions = "Breathe deeply and relax.", animation = ExerciseAnim.BREATHING),
                Exercise("st2e2", "Cobra Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 10, instructions = "Stretch your abs and chest.", animation = ExerciseAnim.GENERIC),
                Exercise("st2e3", "Forward Fold", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 0, instructions = "Reach for toes and relax neck.", animation = ExerciseAnim.GENERIC),
                Exercise("st2e4", "Quad Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 30, restSeconds = 10, instructions = "Pull one heel toward your glutes, keeping knees together.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "st4",
            name = "Deep Stretch Flexibility",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 18,
            calories = 75,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Hips", "Hamstrings", "Back"),
            description = "Longer holds for improved range of motion.",
            thumbnailUrl = "workout_yoga_silhouette",
            exercises = listOf(
                Exercise("st4e1", "Pigeon Pose", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 15, instructions = "Deep hip opener.", animation = ExerciseAnim.GENERIC),
                Exercise("st4e2", "Splits Progression", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Gentle hamstring and hip flexor stretch.", animation = ExerciseAnim.LUNGE),
                Exercise("st4e3", "Deep Lunge Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Stretch hip flexors deeply.", animation = ExerciseAnim.LUNGE),
                Exercise("st4e4", "Seated Spinal Twist", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 15, instructions = "Sit tall and rotate your torso, using your arm to deepen the twist gently.", animation = ExerciseAnim.GENERIC),
                Exercise("st4e5", "Butterfly Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 15, instructions = "Soles of feet together, gently press knees toward the floor.", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "st5",
            name = "Athlete Flexibility Routine",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.ADVANCED,
            durationMin = 20,
            calories = 110,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Hips", "Shoulders"),
            description = "Active mobility and PNF stretching for high-performance recovery.",
            thumbnailUrl = "workout_yoga_sunset",
            exercises = listOf(
                Exercise("st5e1", "Loaded Stretches", ExerciseType.REPS, sets = 3, reps = 10, restSeconds = 10, instructions = "Use light weights to pull into deeper stretch ranges.", easierModification = "Use no weights.", harderModification = "Increase the weight load.", targetMuscle = "Full Body", animation = ExerciseAnim.SQUAT),
                Exercise("st5e2", "PNF Hamstring Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 10, instructions = "Contract and relax hamstring during stretch.", easierModification = "Passive stretching without contraction.", harderModification = "Increase contraction duration.", targetMuscle = "Legs", animation = ExerciseAnim.GENERIC),
                Exercise("st5e3", "Active Mobility Drills", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 10, instructions = "Side to side deep lunges and arm circles.", easierModification = "Reduced range of motion.", harderModification = "Hold bottom positions for 3s.", targetMuscle = "Full Body", animation = ExerciseAnim.LUNGE),
                Exercise("st5e4", "Standing Quad Pull with Reach", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 10, instructions = "Pull one heel toward your glutes while reaching the free arm overhead.", easierModification = "Hold a wall for balance.", harderModification = "Close your eyes to challenge balance.", targetMuscle = "Legs", animation = ExerciseAnim.GENERIC),
                Exercise("st5e5", "90/90 Hip Switch", ExerciseType.REPS, sets = 3, reps = 10, restSeconds = 10, instructions = "Rotate through 90/90 hip positions, controlling the movement with your core.", easierModification = "Use hands to assist the transition.", harderModification = "Lift hands off the floor throughout.", targetMuscle = "Hips", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "st6",
            name = "Total Body Restoration",
            category = WorkoutCategory.STRETCHING,
            difficulty = Difficulty.EXPERT,
            durationMin = 25,
            calories = 180,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Full Body", "Hips", "Shoulders"),
            description = "Advanced PNF and loaded stretching to break through flexibility plateaus.",
            thumbnailUrl = "workout_yoga_sunset",
            exercises = listOf(
                Exercise("st6e1", "Isometric Hamstring Stretch", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "Stretch your hamstring and then contract it against resistance for 5 seconds.", easierModification = "Standard static hamstring stretch.", harderModification = "Increase the contraction intensity.", targetMuscle = "Hamstrings", animation = ExerciseAnim.GENERIC),
                Exercise("st6e2", "Loaded Butterfly Stretch", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "In a butterfly stretch, place light dumbbells on your knees to pull them down.", easierModification = "Standard butterfly stretch.", harderModification = "Gently push down with your hands as well.", targetMuscle = "Hips", animation = ExerciseAnim.GENERIC),
                Exercise("st6e3", "Weighted Shoulder Dislocates", ExerciseType.REPS, sets = 5, reps = 15, restSeconds = 10, instructions = "Hold a light weight and move your arms in a full circle over your head to your back.", easierModification = "Use a towel or PVC pipe instead of weight.", harderModification = "Narrow your grip on the weight.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("st6e4", "Deep Squat Weighted Reach", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "Hold a deep squat and reach a dumbbell overhead with one arm, then the other.", easierModification = "Reach without weight.", harderModification = "Hold both arms overhead simultaneously.", targetMuscle = "Shoulders", animation = ExerciseAnim.SQUAT),
                Exercise("st6e5", "Eagle Pose Bind Flow", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "Twist arms and legs into eagle pose, then flow into a forward fold.", easierModification = "Hold just the arm or leg bind.", harderModification = "Touch elbows to knees in the fold.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC)
            )
        ),

        // ---------------- MOBILITY ----------------
        Workout(
            id = "m1",
            name = "Foundational Mobility",
            category = WorkoutCategory.MOBILITY,
            difficulty = Difficulty.BEGINNER,
            durationMin = 10,
            calories = 45,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Neck", "Shoulders", "Hips"),
            description = "Essential joint rotations to improve daily movement and reduce stiffness.",
            thumbnailUrl = "workout_yoga_cobra",
            exercises = listOf(
                Exercise("m1e1", "Neck Circles", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 30, instructions = "Slowly rotate your head in a full circle, keeping shoulders down and relaxed.", easierModification = "Side-to-side ear-to-shoulder tilts.", harderModification = "Add gentle hand pressure for deeper stretch.", targetMuscle = "Neck", animation = ExerciseAnim.GENERIC),
                Exercise("m1e2", "Shoulder Rolls", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 30, instructions = "Roll your shoulders up, back, and down in a smooth, circular motion.", easierModification = "Roll one shoulder at a time.", harderModification = "Extend arms out to the side while rolling.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("m1e3", "Hip Circles", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 30, instructions = "Place hands on hips and move your pelvis in a wide circular motion.", easierModification = "Smaller circles or hold onto a wall for balance.", harderModification = "Slowly increase the radius of the circle.", targetMuscle = "Hips", animation = ExerciseAnim.GENERIC),
                Exercise("m1e4", "Cat-Cow", ExerciseType.TIME, sets = 1, durationSeconds = 60, restSeconds = 30, instructions = "On all fours, alternate between arching your back and tucking your chin.", easierModification = "Reduce range of motion in the spine.", harderModification = "Hold each position for 5 seconds.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC),
                Exercise("m1e5", "Ankle Circles", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 30, instructions = "Lift one foot and rotate the ankle in both directions.", easierModification = "Do it while seated in a chair.", harderModification = "Draw alphabet letters with your toes.", targetMuscle = "Calves", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "m2",
            name = "Flow & Release",
            category = WorkoutCategory.MOBILITY,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 15,
            calories = 90,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Hips", "Back"),
            description = "Dynamic stretches to unlock tight muscles and improve functional range.",
            thumbnailUrl = "workout_yoga_advanced",
            exercises = listOf(
                Exercise("m2e1", "World's Greatest Stretch", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 25, instructions = "Step into a deep lunge, place inside hand down, and rotate outside arm up.", easierModification = "Drop the back knee to the ground.", harderModification = "Keep the back leg straight and active.", targetMuscle = "Full Body", animation = ExerciseAnim.LUNGE),
                Exercise("m2e2", "Deep Squat with Rotation", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 25, instructions = "Hold a deep squat and reach one arm to the ceiling, alternating sides.", easierModification = "Hold onto a sturdy object for support.", harderModification = "Push knees out with elbows while rotating.", targetMuscle = "Hips", animation = ExerciseAnim.SQUAT),
                Exercise("m2e3", "Thread the Needle", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 25, instructions = "On all fours, slide one arm under your chest and rest your shoulder down.", easierModification = "Don't go all the way to the floor.", harderModification = "Extend the top arm forward.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("m2e4", "Walkouts", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 30, instructions = "From standing, fold forward and walk hands out to a plank, then back.", easierModification = "Bend knees as much as needed.", harderModification = "Add a push-up at the bottom.", targetMuscle = "Full Body", animation = ExerciseAnim.PLANK),
                Exercise("m2e5", "Adductor Rock-backs", ExerciseType.TIME, sets = 2, durationSeconds = 60, restSeconds = 25, instructions = "On all fours with one leg extended to the side, rock hips back to heel.", easierModification = "Keep hips higher during the rock.", harderModification = "Point toes of the straight leg up.", targetMuscle = "Hips", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "m3",
            name = "Athletic Movement Prep",
            category = WorkoutCategory.MOBILITY,
            difficulty = Difficulty.ADVANCED,
            durationMin = 18,
            calories = 140,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Legs", "Hips", "Core"),
            description = "High-level mobility drills designed for athletes and active individuals.",
            thumbnailUrl = "workout_fitness_class",
            exercises = listOf(
                Exercise("m3e1", "Lateral Squats", ExerciseType.REPS, sets = 3, reps = 15, restSeconds = 20, instructions = "Shift weight side to side in a wide stance, keeping one leg straight.", easierModification = "Don't go as deep into the squat.", harderModification = "Hold the bottom for 2 seconds.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("m3e2", "Spiderman Lunges", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 20, instructions = "Step foot outside hand in plank, then reach same-side arm up.", easierModification = "Keep back knee on the floor.", harderModification = "Add a knee-to-elbow drive between reps.", targetMuscle = "Hips", animation = ExerciseAnim.LUNGE),
                Exercise("m3e3", "Bear Crawls", ExerciseType.TIME, sets = 3, durationSeconds = 45, restSeconds = 20, instructions = "Move on all fours with knees hovering just off the ground.", easierModification = "High-hip bear crawl (knees higher).", harderModification = "Backward bear crawl.", targetMuscle = "Full Body", animation = ExerciseAnim.GENERIC),
                Exercise("m3e4", "Frogger Squat to Stand", ExerciseType.REPS, sets = 3, reps = 15, restSeconds = 20, instructions = "In a deep squat, place hands on feet and straighten legs while holding feet.", easierModification = "Use a wider stance.", harderModification = "Keep chest up during the squat phase.", targetMuscle = "Hamstrings", animation = ExerciseAnim.SQUAT),
                Exercise("m3e5", "Scorpions", ExerciseType.REPS, sets = 3, reps = 12, restSeconds = 20, instructions = "Lying face down, reach one foot across your body toward the opposite hand.", easierModification = "Smaller range of motion.", harderModification = "Hold the stretch for 3 seconds.", targetMuscle = "Back", animation = ExerciseAnim.GENERIC)
            )
        ),
        Workout(
            id = "m4",
            name = "Loaded Functional Mobility",
            category = WorkoutCategory.MOBILITY,
            difficulty = Difficulty.EXPERT,
            durationMin = 22,
            calories = 200,
            equipment = Equipment.DUMBBELLS,
            targetMuscleGroups = listOf("Full Body", "Shoulders", "Hips"),
            description = "Expert-level routine using light weights to force muscles into deep, stable ranges.",
            thumbnailUrl = "workout_dumbbell_row",
            exercises = listOf(
                Exercise("m4e1", "Loaded Shoulder Rotations", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 15, instructions = "Hold light dumbbells with elbows at 90 degrees and rotate shoulders.", easierModification = "Use very light weights or no weight.", harderModification = "Perform while standing on one leg.", targetMuscle = "Shoulders", animation = ExerciseAnim.GENERIC),
                Exercise("m4e2", "Weighted Deep Squat Holds", ExerciseType.TIME, sets = 5, durationSeconds = 60, restSeconds = 10, instructions = "Hold a dumbbell at your chest while sitting in the bottom of a squat.", easierModification = "Use a lighter weight.", harderModification = "Add gentle side-to-side prying.", targetMuscle = "Hips", animation = ExerciseAnim.SQUAT),
                Exercise("m4e3", "Cossack Squats", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 15, instructions = "Deep lateral lunge while holding a weight at your chest.", easierModification = "Do not use weight.", harderModification = "Hold the weight straight out in front.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("m4e4", "Overhead Duck Walks", ExerciseType.TIME, sets = 5, durationSeconds = 45, restSeconds = 15, instructions = "In a deep squat, walk forward while holding weights overhead.", easierModification = "Hold weights at shoulder height.", harderModification = "Lower the squat depth.", targetMuscle = "Full Body", animation = ExerciseAnim.SQUAT),
                Exercise("m4e5", "Renegade Row to Rotation", ExerciseType.REPS, sets = 5, reps = 16, restSeconds = 15, instructions = "In plank, row a dumbbell then rotate into a side plank with the weight up.", easierModification = "Perform on knees.", harderModification = "Add a push-up between reps.", targetMuscle = "Core", animation = ExerciseAnim.PLANK)
            )
        ),

        // ---------------- QUICK ----------------
        Workout(
            id = "q1",
            name = "5-Minute Wake Up",
            category = WorkoutCategory.QUICK,
            difficulty = Difficulty.BEGINNER,
            durationMin = 5,
            calories = 35,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body"),
            description = "A rapid-fire circuit to shake off sleep and get your blood flowing.",
            thumbnailUrl = "workout_running_prep",
            exercises = listOf(
                Exercise("q1e1", "Jumping Jacks", ExerciseType.TIME, sets = 1, durationSeconds = 45, restSeconds = 30, instructions = "Jump feet out while swinging arms overhead.", easierModification = "Step side to side instead of jumping.", harderModification = "Increase speed significantly.", targetMuscle = "Full Body", animation = ExerciseAnim.JUMPING_JACKS),
                Exercise("q1e2", "High Knees", ExerciseType.TIME, sets = 1, durationSeconds = 30, restSeconds = 30, instructions = "Run in place, driving knees up toward your chest.", easierModification = "March in place with high knees.", harderModification = "Pump arms faster.", targetMuscle = "Legs", animation = ExerciseAnim.RUN),
                Exercise("q1e3", "Squats", ExerciseType.REPS, sets = 1, reps = 12, restSeconds = 30, instructions = "Lower hips as if sitting in a chair, then stand up.", easierModification = "Squat to a chair or bench.", harderModification = "Hold at the bottom for 3 seconds.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("q1e4", "Standing Crunches", ExerciseType.REPS, sets = 1, reps = 15, restSeconds = 30, instructions = "Bring opposite elbow to knee while standing.", easierModification = "Smaller range of motion.", harderModification = "Add a small hop between reps.", targetMuscle = "Abs", animation = ExerciseAnim.CORE)
            )
        ),
        Workout(
            id = "q2",
            name = "Quick Energy Burst",
            category = WorkoutCategory.QUICK,
            difficulty = Difficulty.INTERMEDIATE,
            durationMin = 10,
            calories = 95,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "Double the time, double the intensity. Perfect for a midday reset.",
            thumbnailUrl = "workout_fitness_class",
            exercises = listOf(
                Exercise("q2e1", "Burpees", ExerciseType.REPS, sets = 2, reps = 12, restSeconds = 25, instructions = "Drop to a plank, do a push-up, jump back up, and leap.", easierModification = "Step back to plank instead of jumping.", harderModification = "Add a tuck jump at the end.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP),
                Exercise("q2e2", "Mountain Climbers", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 25, instructions = "In plank, drive knees rapidly toward your chest.", easierModification = "Slow down the pace.", harderModification = "Bring knees to opposite elbows.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("q2e3", "Lunges", ExerciseType.REPS, sets = 2, reps = 16, restSeconds = 25, instructions = "Step forward and lower your back knee toward the floor.", easierModification = "Hold a wall for balance.", harderModification = "Add a jump when switching legs.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("q2e4", "Plank Jacks", ExerciseType.TIME, sets = 2, durationSeconds = 45, restSeconds = 25, instructions = "In plank position, jump feet in and out like a jumping jack.", easierModification = "Step feet out one at a time.", harderModification = "Move faster while keeping hips still.", targetMuscle = "Core", animation = ExerciseAnim.PLANK),
                Exercise("q2e5", "Push-Ups", ExerciseType.REPS, sets = 2, reps = 15, restSeconds = 25, instructions = "Lower chest to the floor and push back up.", easierModification = "Perform on knees.", harderModification = "Lift one leg during the push-up.", targetMuscle = "Chest", animation = ExerciseAnim.PUSH_UP)
            )
        ),
        Workout(
            id = "q3",
            name = "The 8-Minute Burn",
            category = WorkoutCategory.QUICK,
            difficulty = Difficulty.ADVANCED,
            durationMin = 8,
            calories = 105,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "High density, low rest. Designed to maximize calorie burn in minimum time.",
            thumbnailUrl = "workout_barbell",
            exercises = listOf(
                Exercise("q3e1", "Sprint in Place", ExerciseType.TIME, sets = 4, durationSeconds = 45, restSeconds = 15, instructions = "Run in place at maximum possible speed.", easierModification = "High knee march at fast pace.", harderModification = "High knee sprint with arm pumps.", targetMuscle = "Heart", animation = ExerciseAnim.RUN),
                Exercise("q3e2", "Tuck Jumps", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 15, instructions = "Jump as high as possible and bring knees to chest.", easierModification = "Regular squat jumps.", harderModification = "No pause between jumps.", targetMuscle = "Legs", animation = ExerciseAnim.SQUAT),
                Exercise("q3e3", "Diamond Push-Ups", ExerciseType.REPS, sets = 4, reps = 15, restSeconds = 15, instructions = "Place hands together in a diamond shape and perform push-ups.", easierModification = "Diamond push-ups on knees.", harderModification = "Elevate your feet.", targetMuscle = "Arms", animation = ExerciseAnim.PUSH_UP),
                Exercise("q3e4", "Bicycle Crunches", ExerciseType.REPS, sets = 4, reps = 30, restSeconds = 15, instructions = "Lie on back and pedal legs while bringing opposite elbow to knee.", easierModification = "Keep feet on floor between reps.", harderModification = "Slow down for more tension.", targetMuscle = "Abs", animation = ExerciseAnim.CORE),
                Exercise("q3e5", "Burpees", ExerciseType.TIME, sets = 4, durationSeconds = 45, restSeconds = 15, instructions = "Perform burpees with maximum speed and effort.", easierModification = "No push-up burpees.", harderModification = "Add a double jump at the top.", targetMuscle = "Full Body", animation = ExerciseAnim.PUSH_UP)
            )
        ),
        Workout(
            id = "q4",
            name = "Pure Intensity",
            category = WorkoutCategory.QUICK,
            difficulty = Difficulty.EXPERT,
            durationMin = 6,
            calories = 105,
            equipment = Equipment.NONE,
            targetMuscleGroups = listOf("Full Body", "Heart"),
            description = "Non-stop expert movement. Six minutes of absolute focus and effort.",
            thumbnailUrl = "workout_deadlift",
            exercises = listOf(
                Exercise("q4e1", "Jumping Lunges", ExerciseType.TIME, sets = 5, durationSeconds = 45, restSeconds = 10, instructions = "Switch legs in mid-air with explosive jumps.", easierModification = "Fast alternating lunges.", harderModification = "Jump higher and sink deeper.", targetMuscle = "Legs", animation = ExerciseAnim.LUNGE),
                Exercise("q4e2", "Clap Push-Ups", ExerciseType.REPS, sets = 5, reps = 15, restSeconds = 10, instructions = "Push off explosively and clap hands before landing.", easierModification = "Explosive push-ups (no clap).", harderModification = "Double clap push-ups.", targetMuscle = "Chest", animation = ExerciseAnim.PUSH_UP),
                Exercise("q4e3", "Plank to Sprint", ExerciseType.TIME, sets = 5, durationSeconds = 45, restSeconds = 10, instructions = "Start in plank, jump to feet and sprint for 5 seconds, repeat.", easierModification = "Step up instead of jumping.", harderModification = "Add a burpee before the sprint.", targetMuscle = "Full Body", animation = ExerciseAnim.RUN),
                Exercise("q4e4", "V-Ups", ExerciseType.REPS, sets = 5, reps = 20, restSeconds = 10, instructions = "Lie on back and touch toes with hands by lifting torso and legs.", easierModification = "Single-leg V-ups.", harderModification = "Keep legs straight and off floor.", targetMuscle = "Abs", animation = ExerciseAnim.CORE),
                Exercise("q4e5", "High Knee Sprint", ExerciseType.TIME, sets = 5, durationSeconds = 45, restSeconds = 10, instructions = "Absolute max effort sprint in place.", easierModification = "Lower the knee height.", harderModification = "Fastest pace possible.", targetMuscle = "Heart", animation = ExerciseAnim.RUN)
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
        Achievement("a8", "Explorer", "Complete a workout in every category", "explore", false),
        Achievement("a9", "Early Bird", "Complete a workout before 8 AM", "wb_sunny", false),
        Achievement("a10", "Night Owl", "Log activity after 10 PM", "bedtime", false),
        Achievement("a11", "Steady Progress", "Meet your goal 3 days in a row", "trending_up", false),
        Achievement("a12", "Mountain Climber", "Log 500 lifetime workout minutes", "insights", false)
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
                1 to "c0", // Gentle Start Cardio Walk
                2 to "st2", // Post-Workout Cooldown
                3 to "s0", // Foundation Strength Basics
                4 to "m1", // Foundational Mobility
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
                1 to "s1", // Upper Body Dumbbell Builder
                3 to "s8", // Lower Body & Glute Sculpt
                5 to "s7", // Explosive Upper Body Power
                6 to "s5"  // Advanced Compound Circuit
            )
        ),
        WorkoutPlan(
            id = "p3",
            name = "Mobility & Recovery Focus",
            description = "Unlock your joints and improve your range of motion.",
            durationDays = 7,
            workoutsPerWeek = 7,
            schedule = mapOf(
                1 to "m1",
                2 to "st2",
                3 to "m2",
                4 to "st4",
                5 to "m3",
                6 to "st5",
                7 to "m4"
            )
        ),
        WorkoutPlan(
            id = "p4",
            name = "Quick Daily Movement",
            description = "Just a few minutes a day to stay active and energized.",
            durationDays = 7,
            workoutsPerWeek = 7,
            schedule = mapOf(
                1 to "q1",
                2 to "q2",
                3 to "q1",
                4 to "q3",
                5 to "q2",
                6 to "q4",
                7 to "q1"
            )
        )
    )

    fun getExerciseById(id: String): Exercise? = masterExercises.find { it.id == id }
}