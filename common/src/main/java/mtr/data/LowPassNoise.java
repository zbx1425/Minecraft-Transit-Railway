package mtr.data;


import cn.zbx1425.mtrsteamloco.Main;
import net.minecraft.util.Mth;

import java.util.Random;

public class LowPassNoise {

    private static final int SAMPLE_PER_METER = 4;

    public double stdDev = 0;
    public double ePow = 0;
    private final Random random = new Random();

    private int head;
    private double headDist;
    private final double[] noiseLoopBuffer;

    public LowPassNoise(double cutoffFreq, double stdDev, double bufferLength) {
        this.stdDev = stdDev / ((2 * Math.PI) / cutoffFreq);
        this.ePow = 1 - Math.exp(-1.0 / SAMPLE_PER_METER * ((2 * Math.PI) / cutoffFreq));
        this.noiseLoopBuffer = new double[(int)((bufferLength + 20) * SAMPLE_PER_METER)];
        this.headDist = Double.NEGATIVE_INFINITY;
    }

    public void tick(double newDist) {
        if (Math.abs(newDist - headDist) > noiseLoopBuffer.length / 2.0 / SAMPLE_PER_METER) {
            noiseLoopBuffer[0] = random.nextGaussian() * stdDev;
            for (int i = 1; i < noiseLoopBuffer.length; i++) {
                noiseLoopBuffer[i] = noiseLoopBuffer[i - 1] + ePow * (random.nextGaussian() * stdDev - noiseLoopBuffer[i - 1]);
            }
            head = 0;
            headDist = newDist;
        }
        if (newDist > headDist) {
            for (; headDist < newDist; headDist += 1.0 / SAMPLE_PER_METER) {
                double result = noiseLoopBuffer[head] + ePow * (random.nextGaussian() * stdDev - noiseLoopBuffer[head]);
                head = (head + 1) % noiseLoopBuffer.length;
                noiseLoopBuffer[head] = result;
            }
        } else if (newDist < headDist) {
            for (; headDist > newDist; headDist -= 1.0 / SAMPLE_PER_METER) {
                double result = noiseLoopBuffer[head] + ePow * (random.nextGaussian() * stdDev - noiseLoopBuffer[head]);
                head = (head - 1 + noiseLoopBuffer.length) % noiseLoopBuffer.length;
                // Insert new value at tail, not head
                noiseLoopBuffer[(head + 1) % noiseLoopBuffer.length] = result;
            }
        }
    }

    public double getAt(double dist) {
        double offset = headDist - dist;
        int prevEntry = (head - (int)(offset * SAMPLE_PER_METER) + noiseLoopBuffer.length * 2) % noiseLoopBuffer.length;
        int nextEntry = (prevEntry + 1) % noiseLoopBuffer.length;
        if (prevEntry < 0 || nextEntry < 0) {
            return 0;
        }
        double k = Mth.frac(offset * SAMPLE_PER_METER);
        return (1.0 - k) * noiseLoopBuffer[prevEntry] + k * noiseLoopBuffer[nextEntry];
    }
}
