package com.lzf.hotelcheckinsystem.util;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 可复用的适配器
 */
public abstract class ReusableAdapter<T> extends BaseAdapter {
    private List<T> listData;
    private int listItemResource; // id

    public ReusableAdapter(List<T> listData, int listItemResource) {
        this.listData = listData;
        this.listItemResource = listItemResource;
    }

    public int getCount() {
        // 如果listDara不为空，则返回listData.size;否则返回0
        return (listData != null) ? listData.size() : 0;
    }

    public Object getItem(int position) {
        return listData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * View AnimalAdapter.getView(int position, View convertView, ViewGroup
     * parent) 其实这个convertView是系统提供给我们的可供服用的View 的缓存对象
     * <p>
     * 有多少列就会调用多少次getView(有多少个Item，那么getView方法就会被调用多少次)
     */
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.bind(parent.getContext(), convertView,
                parent, listItemResource, position);
        bindView(holder, (T) getItem(position));
        return holder.getItemView();
    }

    /**
     * 定义一个抽象方法，完成ViewHolder与相关数据集的绑定
     * <p>
     * 我们创建新的BaseAdapter的时候，实现这个方法就好，另外，别忘了把我们自定义 的BaseAdapter改成abstact抽象的！
     */
    public abstract void bindView(ViewHolder holder, T obj);

    /**
     * ViewHolder功能：
     * <p>
     * 1. findViewById，设置控件状态；
     * <p>
     * 2. 定义一个查找控件的方法，我们的思路是通过暴露公共的方法，调用方法时传递过来 控件id，以及设置的内容，比如TextView设置文本：
     * public ViewHolder setText(int id, CharSequence text){文本设置}
     * <p>
     * 3. 将convertView复用部分搬到这里，那就需要传递一个context对象了，我们把需要获取 的部分都写到构造方法中！
     * <p>
     * 4. 写一堆设置方法(public)，比如设置文字大小颜色，图片背景等！
     */
    public static class ViewHolder {
        // 存储ListView 的 item中的View
        private SparseArray<View> viewsOflistViewItem;
        private View storeConvertView; // 存放convertView
        private int position; // 位置、定位
        private Context context; // Context上下文

        // 构造方法，完成相关初始化
        private ViewHolder(Context context, ViewGroup parent,
                           int listItemResource) {
            // 存储ListView 的 item中的View
            viewsOflistViewItem = new SparseArray<View>();
            this.context = context;
            // View android.view.LayoutInflater.inflate(int resource, ViewGroup
            // root, boolean attachToRoot)【LayoutInflater：布局填充器】
            View convertView = LayoutInflater.from(context).inflate(
                    listItemResource, parent, false);
            convertView.setTag(this);
            storeConvertView = convertView; // 存放convertView
        }

        // 绑定ViewHolder与item
        public static ViewHolder bind(Context context, View convertView,
                                      ViewGroup parent, int listItemResource, int position) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(context, parent, listItemResource);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.storeConvertView = convertView;
            }
            holder.position = position;
            return holder;
        }

        // 根据id获取集合中保存的控件
        @SuppressWarnings("unchecked")
        public <T extends View> T getView(int id) {
            T t = (T) viewsOflistViewItem.get(id);
            if (t == null) {
                t = (T) storeConvertView.findViewById(id);
                viewsOflistViewItem.put(id, t);
            }
            return t;
        }

        // 接着我们再定义一堆暴露出来的方法
        // 获取当前条目
        public View getItemView() {
            return storeConvertView;
        }

        // 获取条目位置
        public int getItemPosition() {
            return position;
        }

        // 设置文字
        public ViewHolder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        // 设置TextView中的文本对齐
        public ViewHolder setTVGravity(int id, int gravity) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setGravity(gravity);
            }
            return this;
        }

        // 设置TextView中的文本颜色
        public ViewHolder setTextColor(int id, int color) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            }
            return this;
        }

        // 设置图片
        public ViewHolder setImageResource(int id, int drawableRes) {
            View view = getView(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableRes);
            } else {
                view.setBackgroundResource(drawableRes);
            }
            return this;
        }

        // 设置点击监听
        public ViewHolder setOnClickListener(int id,
                                             View.OnClickListener listener) {
            getView(id).setOnClickListener(listener);
            return this;
        }

        // 设置可见
        public ViewHolder setVisibility(int id, int visible) {
            getView(id).setVisibility(visible);
            return this;
        }

        // 设置标签
        public ViewHolder setTag(int id, Object obj) {
            getView(id).setTag(obj);
            return this;
        }

        // 其他方法可自行扩展
    }

    /**
     * 测试ListView的数据更新(why?????)
     * <p>
     * 删除所有数据
     * <p>
     * public void clear() {
     * <p>
     * if (listData != null) {
     * <p>
     * listData.clear();
     * <p>
     * }
     * <p>
     * notifyDataSetChanged();
     * <p>
     * }
     */

    public void add(T data) {
        listData.add(data);
        notifyDataSetChanged();
    }

    public void delete(T data) {
        listData.remove(data);
        notifyDataSetChanged();
    }
}
