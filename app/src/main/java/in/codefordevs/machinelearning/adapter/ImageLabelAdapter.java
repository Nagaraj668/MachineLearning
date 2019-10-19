package in.codefordevs.machinelearning.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.codefordevs.machinelearning.R;
import in.codefordevs.machinelearning.activity.ImageLabelActivity;
import in.codefordevs.machinelearning.model.ImageLabelResult;

public class ImageLabelAdapter extends RecyclerView.Adapter<ImageLabelAdapter.ViewHolder> {

    private ImageLabelActivity context;
    private List<ImageLabelResult> imageLabelResults;

    public ImageLabelAdapter(ImageLabelActivity context) {
        this.context = context;
    }

    public List<ImageLabelResult> getImageLabelResults() {
        return imageLabelResults;
    }

    public void setImageLabelResults(List<ImageLabelResult> imageLabelResults) {
        this.imageLabelResults = imageLabelResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(context.getLayoutInflater().inflate(R.layout.item_label_result,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ImageLabelResult imageLabelResult = imageLabelResults.get(position);

        holder.labelTextView.setText(imageLabelResult.getLabel());
        holder.confidenceTextView.setText(
                String.valueOf(imageLabelResult.getConfidencePercent()));
    }

    @Override
    public int getItemCount() {
        return imageLabelResults.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView labelTextView;
        TextView confidenceTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            labelTextView = itemView.findViewById(R.id.label);
            confidenceTextView = itemView.findViewById(R.id.confidence);
        }
    }
}
