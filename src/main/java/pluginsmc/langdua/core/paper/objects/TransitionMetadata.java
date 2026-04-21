package pluginsmc.langdua.core.paper.objects;

public class TransitionMetadata {
    private TransitionEffect effect = TransitionEffect.NONE;
    private int durationTicks;
    private int strength = 1;

    public TransitionEffect getEffect() {
        return effect == null ? TransitionEffect.NONE : effect;
    }

    public void setEffect(TransitionEffect effect) {
        this.effect = effect == null ? TransitionEffect.NONE : effect;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(0, durationTicks);
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = Math.max(0, strength);
    }
}
