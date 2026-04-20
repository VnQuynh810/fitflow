/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { Exercise } from './types';

export const DEFAULT_EXERCISES: Exercise[] = [
  {
    id: 'pushup',
    name: 'Push Ups',
    reps: 15,
    sets: 3,
    caloriesBurned: 50,
    category: 'Strength',
    description: 'Basic push ups for chest and triceps.'
  },
  {
    id: 'squat',
    name: 'Squats',
    reps: 20,
    sets: 3,
    caloriesBurned: 60,
    category: 'Strength',
    description: 'Lower body exercise for quads and glutes.'
  },
  {
    id: 'plank',
    name: 'Plank',
    duration: 60,
    sets: 3,
    caloriesBurned: 40,
    category: 'Strength',
    description: 'Core stability exercise.'
  },
  {
    id: 'jumping-jacks',
    name: 'Jumping Jacks',
    duration: 120,
    sets: 1,
    caloriesBurned: 100,
    category: 'Cardio',
    description: 'Full body cardio exercise.'
  },
  {
    id: 'burpees',
    name: 'Burpees',
    reps: 10,
    sets: 3,
    caloriesBurned: 120,
    category: 'Cardio',
    description: 'High intensity full body exercise.'
  },
  {
    id: 'yoga-stretch',
    name: 'Morning Stretch',
    duration: 300,
    sets: 1,
    caloriesBurned: 30,
    category: 'Flexibility',
    description: 'Basic yoga stretches for flexibility.'
  }
];

export const WATER_GOAL = 2500; // ml
export const STEP_GOAL = 10000;
