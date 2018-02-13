package ch.gibmit.straessle.dominik.memory_3;

import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by dominik on 02.11.17.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {


    private ArrayList<Card> cards;
    private MainActivity context;

    public MyAdapter(ArrayList<Card> cards, MainActivity context) {
        this.cards = cards;
        this.context = context;
    }

    //Implementierte Methoden
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        if (position > cards.size()) {
            if (cards.get(position).getText().equals("") && cards.get(position).getImagePath().equals("")) {
                holder.image.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                holder.text.setText("No Text");
            } else {
                holder.image.setImageBitmap(BitmapFactory.decodeFile(cards.get(position).getImagePath()));
                holder.text.setText(cards.get(position).getText());
            }
        } else {
            holder.image.setImageBitmap(BitmapFactory.decodeFile(cards.get(position).getImagePath()));
            holder.text.setText(cards.get(position).getText());
        }

        holder.image.setRotation(90f);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }


    //Innere Klasse
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView text;
        ImageView image;

        MyHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            text = (TextView) itemView.findViewById(R.id.cardText);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
            text.setTextSize(10f);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            context.takeQrCodePicture();
            context.setCurrentPosition(getAdapterPosition());
        }
    }

    public void setCard(String text, String imagePath, int position) {
        cards.get(position).setText(text);
        cards.get(position).setImagePath(imagePath);
    }

}
