/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { GoogleGenAI, Type } from "@google/genai";
import { UserStats, WorkoutPlan, Exercise } from "../types";
import { DEFAULT_EXERCISES } from "../constants";

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

export async function generate30DayPlan(stats: UserStats): Promise<WorkoutPlan> {
  const prompt = `
    Generate a 30-day workout plan for a user with the following stats:
    - Height: ${stats.height}cm
    - Weight: ${stats.weight}kg
    - BMI: ${stats.bmi.toFixed(1)}
    - Goal: ${stats.goal}
    
    The plan should include a list of exercises for each day. 
    Some days can be rest days.
    Available exercises represent a baseline, but you can suggest others if common.
    Format the response as a JSON object matching the WorkoutPlan structure.
  `;

  try {
    const response = await ai.models.generateContent({
      model: "gemini-3-flash-preview",
      contents: prompt,
      config: {
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            title: { type: Type.STRING },
            days: {
              type: Type.ARRAY,
              items: {
                type: Type.OBJECT,
                properties: {
                  dayNumber: { type: Type.INTEGER },
                  exercises: {
                    type: Type.ARRAY,
                    items: {
                      type: Type.OBJECT,
                      properties: {
                        name: { type: Type.STRING },
                        reps: { type: Type.INTEGER },
                        sets: { type: Type.INTEGER },
                        duration: { type: Type.INTEGER },
                        caloriesBurned: { type: Type.INTEGER },
                        category: { type: Type.STRING }
                      }
                    }
                  },
                  isRestDay: { type: Type.BOOLEAN }
                }
              }
            }
          }
        }
      }
    });

    const data = JSON.parse(response.text || "{}");
    
    // Convert relative day numbers to actual dates starting from today
    const now = new Date();
    const days: Record<string, any> = {};
    
    data.days.forEach((day: any, index: number) => {
      const date = new Date(now);
      date.setDate(now.getDate() + index);
      const dateStr = date.toISOString().split('T')[0];
      
      days[dateStr] = {
        date: dateStr,
        exercises: day.isRestDay ? [] : day.exercises.map((e: any) => ({
          ...e,
          id: `ai-${Math.random().toString(36).substr(2, 9)}`
        })),
        isCompleted: false
      };
    });

    return {
      id: `plan-${Date.now()}`,
      startDate: now.toISOString().split('T')[0],
      days,
      title: data.title || `${stats.goal} 30-Day Plan`
    };
  } catch (error) {
    console.error("Failed to generate plan:", error);
    // Return a fallback plan if AI fails
    return createFallbackPlan(stats);
  }
}

function createFallbackPlan(stats: UserStats): WorkoutPlan {
  const now = new Date();
  const days: Record<string, any> = {};
  
  for (let i = 0; i < 30; i++) {
    const date = new Date(now);
    date.setDate(now.getDate() + i);
    const dateStr = date.toISOString().split('T')[0];
    
    const isRestDay = i % 3 === 2; // Every 3rd day is rest
    
    days[dateStr] = {
      date: dateStr,
      exercises: isRestDay ? [] : DEFAULT_EXERCISES.slice(0, 3).map(e => ({...e})),
      isCompleted: false
    };
  }

  return {
    id: 'fallback-plan',
    startDate: now.toISOString().split('T')[0],
    days,
    title: `Standard ${stats.goal} Plan`
  };
}
