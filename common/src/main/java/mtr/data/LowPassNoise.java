package mtr.data;


import net.minecraft.util.Mth;

import java.util.Random;

public class LowPassNoise {

    public double cutoffFreq = 50.0;
    public double stdDev = 0.001;
    private final Random random;
    private double prevX = 0;
    private int baseI;
    private final double[] noiseValue1 = new double[400];
    private final double[] noiseValue2 = new double[400];

    public LowPassNoise() {
        random = new Random();
        resetBuffer();
    }

    public LowPassNoise(double cutoffFreq, double stdDev) {
        this();
        this.cutoffFreq = cutoffFreq;
        this.stdDev = stdDev;
    }

    public void tick(double x) {
        double deltaX = x - prevX;
        prevX = x;
        int prevBaseI = baseI;
        baseI = (int)Math.ceil(x * 4.0);
        if (deltaX == 0.0 || Math.abs(deltaX) >= 100.0) {
            resetBuffer();
            return;
        }
        if (deltaX < 0.0) {
            int crntBase = (prevBaseI + 1) % 400;
            for (int i = prevBaseI; i > baseI; i--) {
                int crntTg = i % 400;
                generateNoise(crntBase, crntTg);
                crntBase = crntTg;
            }
        } else {
            int crntBase = prevBaseI % 400;
            for (int j = prevBaseI + 1; j <= baseI; j++) {
                int crntTg = j % 400;
                generateNoise(crntBase, crntTg);
                crntBase = crntTg;
            }
        }
    }

    private void resetBuffer() {
        for (int i = 0; i < 400; i++) {
            noiseValue1[i] = 0.0;
            noiseValue2[i] = 0.0;
        }
    }

    private void generateNoise(int baseI, int tgI) {
        double omega = Mth.TWO_PI / cutoffFreq;
        double k = 1.0 - Math.exp(-omega / 4.0);
        double normalNoise = stdDev * Math.sqrt(-2.0 * Math.log(random.nextDouble())) * Mth.cos(Mth.TWO_PI * random.nextFloat());
        noiseValue1[tgI] = noiseValue1[baseI] + (normalNoise / omega - noiseValue1[baseI]) * k;
        noiseValue2[tgI] = noiseValue2[baseI] + (noiseValue1[tgI] / omega - noiseValue2[baseI]) * k;
    }

    public double getAt(double x) {
        double reqX = x * 4.0;
        int reqXFloor = (int)Math.floor(reqX);
        double interpolateRatio = reqX - (double)reqXFloor;
        int prevEntry = (reqXFloor + 400) % 400;
        int nextEntry = (reqXFloor + 400 + 1) % 400;
        return (1.0 - interpolateRatio) * noiseValue2[prevEntry] + interpolateRatio * noiseValue2[nextEntry];
    }
}
