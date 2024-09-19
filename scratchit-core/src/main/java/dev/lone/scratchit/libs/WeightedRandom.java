/**
 * MIT License
 * <p>
 * Copyright (c) 2022 Julien Marcuse
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.lone.scratchit.libs;

import dev.lone.scratchit.Main;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Uses a map of outcomes to weights to get random values
 *
 * @param <T> The type of the value
 * @author Redempt
 */
public class WeightedRandom<T>
{

    private Map<T, Double> weights;
    private double total;
    private List<Double> totals;
    private List<T> items;

    /**
     * Create a new WeightedRandom from a map of outcomes to their weights
     *
     * @param map The map of outcomes to their weights
     * @param <T> The type of the outcomes
     * @return A WeightedRandom which can be used to roll for the given outcome
     */
    public static <T> WeightedRandom<T> fromIntMap(Map<T, Integer> map)
    {
        HashMap<T, Double> dmap = new HashMap<>();
        map.forEach((k, v) -> {
            dmap.put(k, (double) v);
        });
        return new WeightedRandom<T>(dmap, false);
    }

    /**
     * Create a new WeightedRandom from a map of outcomes to their weights
     *
     * @param map The map of outcomes to their weights
     * @param <T> The type of the outcomes
     * @return A WeightedRandom which can be used to roll for the given outcome
     */
    public static <T> WeightedRandom<T> fromDoubleMap(Map<T, Double> map)
    {
        return new WeightedRandom<T>(map, false);
    }

    /**
     * Creates a WeightedRandom using the map of weights
     *
     * @param weights The map of outcomes to weights
     * @deprecated Use {@link WeightedRandom#fromIntMap(Map)}
     */
    public WeightedRandom(Map<T, Integer> weights)
    {
        HashMap<T, Double> dmap = new HashMap<>();
        weights.forEach((k, v) -> {
            dmap.put(k, (double) v);
        });
        initialize(dmap);
    }

    /**
     * Create an empty WeightedRandom
     */
    public WeightedRandom()
    {
        weights = new HashMap<>();
        totals = new ArrayList<>();
        items = new ArrayList<>();
        total = 0;
    }

    private WeightedRandom(Map<T, Double> weights, boolean no)
    {
        initialize(weights);
    }

    private void initialize(Map<T, Double> weights)
    {
        this.weights = weights;
        total = 0;
        totals = new ArrayList<>();
        items = new ArrayList<>();
        int[] pos = {0};
        weights.forEach((k, v) -> {
            total += v;
            totals.add(total);
            items.add(k);
            pos[0]++;
        });
    }

    /**
     * Rolls and gets a weighted random outcome
     *
     * @return A weighted random outcome, or null if there are no possible outcomes
     */
    public T roll()
    {
        if (totals.size() == 0)
        {
            return null;
        }
        double random = Math.random() * (total);
        int pos = Collections.binarySearch(totals, random);
        if (pos < 0)
        {
            pos = -(pos + 1);
        }
        pos = Math.min(pos, items.size() - 1);
        return items.get(pos);
    }

    public T roll(Player player)
    {
        if (totals.size() == 0)
        {
            return null;
        }
        double random = Math.random() * (total);
        if (Main.config.getBoolean("debug.log-result-prize-chance"))
            player.sendMessage("Chance " + random);
        int pos = Collections.binarySearch(totals, random);
        if (pos < 0)
        {
            pos = -(pos + 1);
        }
        pos = Math.min(pos, items.size() - 1);
        return items.get(pos);
    }

    /**
     * Gets the chance each outcome has to occur in percentage (0-100)
     *
     * @return A map of each outcome to its percentage chance to occur when calling {@link WeightedRandom#roll()}
     */
    public Map<T, Double> getPercentages()
    {
        Map<T, Double> percentages = new HashMap<>();
        weights.forEach((k, v) -> {
            percentages.put(k, (v / total) * 100d);
        });
        return percentages;
    }

    /**
     * Gets the map of weights for this WeightedRandom
     *
     * @return The weight map
     */
    public Map<T, Double> getWeights()
    {
        return weights;
    }

    /**
     * Sets another weight in this WeightedRandom, replacing the weight of the outcome if it has already been added
     *
     * @param outcome The weight to set
     * @param weight  The outcome to set
     */
    public void set(T outcome, int weight)
    {
        set(outcome, (double) weight);
    }

    /**
     * Sets another weight in this WeightedRandom, replacing the weight of the outcome if it has already been added
     *
     * @param outcome The weight to set
     * @param weight  The outcome to set
     */
    public void set(T outcome, double weight)
    {
        remove(outcome);
        total += weight;
        weights.put(outcome, weight);
        totals.add(total);
        items.add(outcome);
    }

    /**
     * Removes an outcome from this WeightedRandom
     *
     * @param outcome The outcome to remove
     */
    public void remove(T outcome)
    {
        Double value = weights.remove(outcome);
        if (value == null)
        {
            return;
        }
        int index = items.indexOf(outcome);
        items.remove(index);
        totals.remove(index);
        for (int i = index; i < totals.size(); i++)
        {
            totals.set(i, totals.get(i) - value);
        }
    }

    /**
     * Creates a copy of this WeightedRandom
     *
     * @return An identical copy of this WeightedRandom
     */
    public WeightedRandom<T> clone()
    {
        return new WeightedRandom<T>(new HashMap<>(weights), false);
    }

    /**
     * Performs a single roll given a map of outcomes to weights. If you need to roll multiple times, instantiate a WeightedRandom and call roll on that each time instead.
     *
     * @param map The map of outcomes to weights
     * @param <T> The type being returned
     * @return A weighted random outcome
     */
    public static <T> T roll(Map<T, Integer> map)
    {
        return new WeightedRandom<T>(map).roll();
    }

}