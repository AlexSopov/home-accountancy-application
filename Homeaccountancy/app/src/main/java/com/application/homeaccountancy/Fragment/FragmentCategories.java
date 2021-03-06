package com.application.homeaccountancy.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import com.application.homeaccountancy.Data.AccountancyContract;
import com.application.homeaccountancy.R;
import com.application.homeaccountancy.Activity.SingleCategoryActivity;

public class FragmentCategories extends UsingDataBaseListFragment {
    private String query;
    private SimpleCursorAdapter categoriesCursorAdapter;

    // Фабричный метод для создания фрагмента со списком категорий
    // Удовлетворяющих запросу query
    public static FragmentCategories FragmentCategoriesFactory(String query) {
        FragmentCategories fragmentCategories = new FragmentCategories();
        fragmentCategories.setQuery(query +
                " ORDER BY " + AccountancyContract.Category.C_TITLE + " ASC");
        return fragmentCategories;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(Html.fromHtml(getResources().getString(R.string.empty_text)));

        // Зарегистрировать событие создания контекстного меню
        // при долгом нажатии на элемент списка
        registerForContextMenu(getListView());
        initializeAdapter();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint())
            return false;

        // Обработка нажатия на элементе из контекстного меню

        // Id нажатого элемента
        final long id = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).id;
        switch (item.getItemId()) {
            // Если нажата кнопка "Изменить"
            // Вызвать Activity изменения категории и передать Id нажатого элемента
            case R.id.change:
                Intent intent = new Intent(getActivity().getApplicationContext(), SingleCategoryActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                return true;
            case R.id.delete:
                // Если нажата кнопка "Удалить"

                // Создать диалоговое окно для подтверждения действия
                // и удалить элемент в случае необходимости
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog
                        .setTitle("Confirm Action")
                        .setMessage("Do you really want to delete categoty? All relative transactions will be deleted." )
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.delete(AccountancyContract.Category.TABLE_NAME,
                                        AccountancyContract.Category._ID + "=?",
                                        new String[]{String.valueOf(id)}
                                );
                                requeryCursor();
                                categoriesCursorAdapter.changeCursor(cursor);
                                categoriesCursorAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .create();
                dialog.show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        requeryCursor();
        categoriesCursorAdapter.changeCursor(cursor);
    }

    private void initializeAdapter() {
        // Поля для вывода данных
        String[] from = new String[] {
                AccountancyContract.Category.C_TITLE,
                AccountancyContract.Category.ICON
        };

        // Элементы, куда будут выводиться данные
        int[] to = new int[] {
                R.id.category_list_item_title,
                R.id.category_list_item_icon
        };

        // Обновление данных в курсоре
        requeryCursor();

        // Инициализация адаптера
        categoriesCursorAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                R.layout.category_list_item, cursor, from, to, 0);
        setListAdapter(categoriesCursorAdapter);
    }

    public void setQuery(String query) {
        this.query = query;
    }
    public static String getBaseQuery() {
        return "SELECT * FROM " + AccountancyContract.Category.TABLE_NAME;
    }

    private void requeryCursor() {
        // Обновление данных в курсоре
        cursor =  db.rawQuery(query, null);
    }
}