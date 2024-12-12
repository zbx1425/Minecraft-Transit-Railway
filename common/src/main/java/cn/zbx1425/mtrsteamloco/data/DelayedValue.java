package cn.zbx1425.mtrsteamloco.data;

import it.unimi.dsi.fastutil.floats.FloatArrayFIFOQueue;

public class DelayedValue {

    private int delayTicks;
    private FloatArrayFIFOQueue buffer;
    private float elapsedDeltaTicks;

    public DelayedValue(double delay) {
        setDelay(delay);
    }

    public void setDelay(double delay) {
        this.delayTicks = (int) (delay * 20);
        if (buffer != null) {
            buffer.clear();
        } else {
            buffer = new FloatArrayFIFOQueue(delayTicks);
        }
        elapsedDeltaTicks = 0;
    }

    public float setAndGet(float newValue, float deltaTicks) {
        elapsedDeltaTicks += deltaTicks;
        if (elapsedDeltaTicks < 1) return buffer.isEmpty() ? newValue : buffer.firstFloat();
        elapsedDeltaTicks -= 1;
        buffer.enqueue(newValue);
        if (buffer.size() > delayTicks) {
            buffer.dequeueFloat();
        }
        return buffer.firstFloat();
    }
}
