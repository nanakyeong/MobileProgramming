package com.example.mobileprogramming;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void setBooks(List<Book> books) {
        this.bookList = books;
        notifyDataSetChanged();
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_main, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.textTitle.setText(book.getTitle());

        if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(book.getImagePath())
                    .into(holder.imageCover);
        } else {
            holder.imageCover.setImageResource(R.drawable.book1);
        }

        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("독후감")
                    .setMessage("제목: " + book.getTitle()
                            + "\n저자: " + book.getAuthor()
                            + "\n\n인상 깊은 구절:\n" + book.getQuote()
                            + "\n\n느낀 점:\n" + book.getThoughts())
                    .setPositiveButton("닫기", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCover;
        TextView textTitle;

        public BookViewHolder(View itemView) {
            super(itemView);
            imageCover = itemView.findViewById(R.id.bookCover);
            textTitle = itemView.findViewById(R.id.bookTitle);
        }
    }
}