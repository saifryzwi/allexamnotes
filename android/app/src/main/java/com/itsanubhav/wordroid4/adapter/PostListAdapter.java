package com.itsanubhav.wordroid4.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import com.itsanubhav.wordroid4.ContainerActivity;
import com.itsanubhav.wordroid4.MainActivity;
import com.itsanubhav.wordroid4.PostContainerActivity;
import com.itsanubhav.wordroid4.R;
import com.itsanubhav.wordroid4.fragment.category.CategoryFragment;
import com.itsanubhav.wordroid4.listeners.OnHomePageItemClickListener;
import com.itsanubhav.wordroid4.others.Utils;
import com.itsanubhav.wordroid4.viewholders.UnifiedNativeAdViewHolder;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.itsanubhav.libdroid.model.LoadMoreViewClass;
import com.itsanubhav.libdroid.model.WorDroidSectionItems;
import com.itsanubhav.libdroid.model.category.Category;
import com.itsanubhav.libdroid.model.post.Post;
import com.itsanubhav.libdroid.model.posts.Posts;
import com.mikepenz.iconics.view.IconicsTextView;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Items
    public static final int LOADING_ITEM = -1;
    public static final int FIRST_ITEM = 0;
    public static final int POST_ITEMS = 1;
    public static final int EMPTY_VIEW = 2;
    public static final int CHILD_RECYCLER = 3;
    public static final int AD_VIEW = 4;
    private static final int ADMOB_NATIVE_AD = 5;

    private Integer parentCategory;
    private String parentCategoryName;
    private boolean isListMode;
    private boolean sliderEnabled;
    private static LoadMoreViewClass loadingItem = LoadMoreViewClass.newInstance();

    private List<Object> itemsList = new ArrayList<>();
    private List<Posts> sliderPost = new ArrayList<>();
    private Context context;
    private OnHomePageItemClickListener postListener;



    public PostListAdapter(Context context, OnHomePageItemClickListener postListener) {
        this.context = context;
        this.postListener = postListener;
    }

    public void addItems(List<Posts> posts){
        int temp = itemsList.size();
        this.itemsList.addAll(posts);
        notifyItemRangeInserted(temp,posts.size());
        addLoadMoreView();
    }

    public void addSubCategories(List<Category> categories,Integer parent){
        int pos = itemsList.size() == 0 ? 0 : 1;
        this.itemsList.add(pos,categories);
        this.parentCategory = parent;
        notifyItemInserted(1);
    }

    public void addAsObject(List<Posts> posts){
        this.itemsList.add(4,posts);
        notifyItemInserted(4);
    }

    public void endOfList(){
        itemsList.remove(loadingItem);
        notifyItemRemoved(itemsList.size());
    }

    public void removeAllViews(){
        itemsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemsList.get(position) instanceof Posts) {
            if (position==0){
                return FIRST_ITEM;
            }else {
                return POST_ITEMS;
            }
        }else if (itemsList.get(position) instanceof List){
                return CHILD_RECYCLER;
        }else if (itemsList.get(position) instanceof LoadMoreViewClass){
            return LOADING_ITEM;
        }if (itemsList.get(position) instanceof UnifiedNativeAd) {
            return ADMOB_NATIVE_AD;
        }else {
            return EMPTY_VIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==FIRST_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_big_picture,parent,false);
            return new BigPictureViewHolder(view,postListener);
        }else if (viewType==POST_ITEMS){
            View view = LayoutInflater.from(parent.getContext()).inflate(isListMode ? R.layout.item_simple_post_list : R.layout.item_big_picture,parent,false);
            return new SimpleItemViewHolder(view,postListener);
        }else if(viewType == ADMOB_NATIVE_AD){
            View unifiedAdLayout = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.admob_ad_item,
                    parent, false);
            return new UnifiedNativeAdViewHolder(unifiedAdLayout);
        }else if (viewType==LOADING_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_more_view_item,parent,false);
            return new LoadingViewHolder(view);
        } else if (viewType == CHILD_RECYCLER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_recyclerview,parent,false);
            return new ChildRecyclerViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int itemType = holder.getItemViewType();
        if (itemType==POST_ITEMS){
            Posts post = (Posts) itemsList.get(position);
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.title.setText(Jsoup.parse(post.getTitle()).text());
            //Adjust the height of the image in grid mode to 120dp
            if (!isListMode){
                viewHolder.imageViewContainer.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, context.getResources().getDisplayMetrics());
            }
            if (post.getFeaturedImg()!=null) {
                Glide.with(context)
                        .load(post.getFeaturedImg())
                        .into(viewHolder.imageView);
            }
            String meta = "";
            if (post.getModified()!=null)
                meta += "  {faw-calendar-day} " + Utils.parseDate(post.getModified());

            meta+= "  {faw-comments} "+post.getCommentCount();

            meta+= "  {faw-eye} "+post.getPostViews();
            
            viewHolder.postMetaView.setText(meta.trim());
            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, PostContainerActivity.class);
                intent.putExtra("postId", post.getId());
                intent.putExtra("img", post.getFeaturedImg());
                intent.putExtra("title", post.getTitle());
                context.startActivity(intent);
            });
            meta = null;
        } else if(itemType == FIRST_ITEM){
            Posts post = (Posts) itemsList.get(position);
            BigPictureViewHolder viewHolder = (BigPictureViewHolder) holder;
            if (!isListMode){
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }
            viewHolder.titleView.setText(Jsoup.parse(post.getTitle()).text());
            if (post.getFeaturedImg()!=null) {
                Glide.with(context)
                        .load(post.getFeaturedImg())
                        .into(viewHolder.imageView);
            }

            String meta = "";
            if (post.getAuthorName()!=null)
                meta = "{faw-user-edit} "+post.getAuthorName();
            if (post.getModified()!=null)
                meta += "  {faw-calendar-day} " + post.getModified();

                meta+= "  {faw-comments} "+post.getCommentCount();

                meta+= "  {faw-eye} "+post.getPostViews();
            viewHolder.postMetaView.setText(meta.trim());
            viewHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, PostContainerActivity.class);
                intent.putExtra("postId", post.getId());
                intent.putExtra("img", post.getFeaturedImg());
                intent.putExtra("title", post.getTitle());
                context.startActivity(intent);
            });
            meta = null;

        } else if (itemType==ADMOB_NATIVE_AD){
            if (!isListMode){
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }
            UnifiedNativeAd nativeAd = (UnifiedNativeAd) itemsList.get(position);
            populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());
        }else if(itemType==CHILD_RECYCLER){
            try {
                @SuppressWarnings("unchecked")
                ChildRecyclerViewHolder childRecyclerViewHolder = (ChildRecyclerViewHolder) holder;
                List<WorDroidSectionItems> worDroidSectionItems   = new ArrayList<>();
                Intent intent = new Intent(context, ContainerActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                for (Object object:(List)itemsList.get(position)){
                    if (object instanceof Category) {
                        childRecyclerViewHolder.childRecyclerTitle.setText("Sub-Categories");
                        Category c = (Category) object;
                        WorDroidSectionItems items = new WorDroidSectionItems();
                        items.setCount(c.getCount());
                        items.setTitle(c.getName());
                        items.setId(c.getId());
                        worDroidSectionItems.add(items);
                        //Setting item type for category
                        itemType = 5;
                        intent.putExtra("title","Categories");
                        intent.putExtra("screen","categories");
                        intent.putExtra("data",parentCategory);
                    } else if (object instanceof Posts){
                        Posts p = (Posts) object;
                        WorDroidSectionItems item = new WorDroidSectionItems();
                        item.setTitle(p.getTitle());
                        item.setAuthorName(p.getAuthorName());
                        item.setCommentCount(p.getCommentCount());
                        item.setFeaturedImg(p.getFeaturedImg());
                        item.setModified(p.getModified());
                        item.setPostViews(p.getPostViews());
                        item.setId(p.getId());
                        worDroidSectionItems.add(item);
                        //Setting item type for post
                        itemType = 4;
                    }

                }

                childRecyclerViewHolder.seeMoreBtn.setOnClickListener(view -> {
                    if (context instanceof MainActivity){
                        context.startActivity(intent);
                    }else if (context instanceof ContainerActivity){
                        try {
                            ContainerActivity a = (ContainerActivity) context;
                            String title = "Sub-Categories";
                            a.addFragment(CategoryFragment.newInstance(null,parentCategory),title);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }


                });
                ChildAdapter adapter = new ChildAdapter(context,postListener);
                adapter.setItemType(itemType);
                adapter.addItems(worDroidSectionItems);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false);
                childRecyclerViewHolder.childRecycler.setLayoutManager(linearLayoutManager);
                childRecyclerViewHolder.childRecycler.setAdapter(adapter);
                /*SnapHelper helper = new LinearSnapHelper();
                helper.attachToRecyclerView(childRecyclerViewHolder.childRecycler);*/
            }catch (Exception e){
                Toast.makeText(context,"Exception in PostListAdapter ",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else if (itemType==LOADING_ITEM){
            if (!isListMode){
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (itemsList!=null)
            return itemsList.size();
        else
            return 0;
    }

    public static class BigPictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleView;
        IconicsTextView postMetaView;
        ImageView imageView;
        CardView imageViewContainer;
        private OnHomePageItemClickListener postListener;

        public BigPictureViewHolder(@NonNull View itemView, OnHomePageItemClickListener postListener) {
            super(itemView);
            titleView = itemView.findViewById(R.id.post_title);
            imageView = itemView.findViewById(R.id.post_image);
            postMetaView = itemView.findViewById(R.id.post_meta);
            imageViewContainer = itemView.findViewById(R.id.post_image_container);
            this.postListener = postListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                postListener.onClick(getAdapterPosition(),"post");
            }catch (Exception ignored){
                ignored.printStackTrace();
            }
        }
    }

    private static class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView title;
        ImageView imageView;
        IconicsTextView postMetaView;
        CardView imageViewContainer;
        OnHomePageItemClickListener onPostListener;

        SimpleItemViewHolder(@NonNull View itemView, OnHomePageItemClickListener postListener) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            imageView = itemView.findViewById(R.id.post_image);
            postMetaView = itemView.findViewById(R.id.post_meta);
            imageViewContainer = itemView.findViewById(R.id.post_image_container);
            onPostListener = postListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                onPostListener.onClick(getAdapterPosition(),"post");
            }catch (Exception ignored){

            }
        }

        @Override
        public boolean onLongClick(View view) {
            onPostListener.onLongClick(getAdapterPosition(),"post");
            return true;
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public void setSlider(List<Posts> sliderPosts){
        this.sliderPost = sliderPosts;
    }

    public void setLayoutManager(boolean isListMode) {
        this.isListMode = isListMode;
    }

    public void addLoadMoreView(){
        itemsList.remove(loadingItem);
        itemsList.add(loadingItem);
        notifyItemInserted(itemsList.size()-1);
    }

    public void insertAdItem(Object object,int position){
        if (position<itemsList.size()) {
            itemsList.add(position, object);
            notifyItemInserted(position);
        }
    }

    private void setupSlider(CarouselView carouselView, List<Post> posts){
        try {
            carouselView.setViewListener(new ViewListener() {
                @Override
                public View setViewForPosition(int position) {
                    Post postsItem = posts.get(position);
                    View customView = LayoutInflater.from(context).inflate(R.layout.slider_item_layout, null);
                    ImageView imageView = customView.findViewById(R.id.sliderImageView);
                    TextView titleView = customView.findViewById(R.id.sliderTitleView);
                    TextView categoryTitle = customView.findViewById(R.id.sliderCategoryTitle);
                    try {
                        Glide.with(context)
                                .load(postsItem.getFeaturedImgUrl())
                                .into(imageView);
                    }catch (Exception e){
                        Glide.with(context)
                                .load(R.color.md_red_200)
                                .into(imageView);
                    }
                    titleView.setText(Jsoup.parse(postsItem.getTitle().getRendered()).text());
                    categoryTitle.setText(Jsoup.parse(postsItem.getCategoriesDetails().get(0).getName()).text());
                    customView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    return customView;
                }
            });
            carouselView.setPageCount(posts.size());
            carouselView.setVisibility(View.VISIBLE);
            // set ViewListener for custom view
        }catch (Exception e){
            carouselView.setVisibility(GONE);
        }
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    public interface OnPostListener{
        void onPostClick(int position);
    }


    private static class ChildRecyclerViewHolder extends RecyclerView.ViewHolder {

        RecyclerView childRecycler;
        Button seeMoreBtn;
        TextView childRecyclerTitle;

        public ChildRecyclerViewHolder(View view) {
            super(view);
            childRecyclerTitle = view.findViewById(R.id.childRecyclerTitle);
            childRecycler = view.findViewById(R.id.childRecyclerView);
            seeMoreBtn = view.findViewById(R.id.seeMore);
        }

    }
}
