package in.codefordevs.machinelearning.model;

public class ImageLabelResult {
    private String label;
    private float confidence;

    public ImageLabelResult(String text, float confidence) {
        this.label = text;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public int getConfidencePercent() {
        return (int) (confidence * 100);
    }
}
