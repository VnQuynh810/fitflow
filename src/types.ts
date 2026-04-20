/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Exercise {
  id: string;
  name: string;
  duration?: number; // seconds
  reps?: number;
  sets?: number;
  caloriesBurned: number;
  category: 'Strength' | 'Cardio' | 'Flexibility' | 'Yoga';
  description: string;
  isCustom?: boolean;
}

export interface WorkoutDay {
  date: string; // ISO format
  exercises: Exercise[];
  isCompleted: boolean;
}

export interface UserStats {
  height: number; // cm
  weight: number; // kg
  bmi: number;
  gender: 'male' | 'female' | 'other';
  goal: 'Weight Loss' | 'Muscle Gain' | 'Endurance' | 'Maintenance';
}

export interface WorkoutPlan {
  id: string;
  startDate: string;
  days: Record<string, WorkoutDay>; // key is date string YYYY-MM-DD
  title: string;
}

export interface DailyActivity {
  steps: number;
  waterIntake: number; // ml
  lastUpdated: string;
}

export interface Streak {
  current: number;
  lastWorkoutDate: string | null;
}
