package com.hanul.myinfra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Optional<Item> findItem(Long id) {
        Optional<Item> result = itemRepository.findById(id);
        return result;
    }

    public void saveItem(String title, Integer price) {
        Item item = new Item();
        item.setTitle(title);
        item.setPrice(price);
        itemRepository.save(item);
    }

    public void updateItem(Long id, String title, Integer price) {
        Optional<Item> findItem = findItem(id);
        Item item = findItem.get();
        item.setTitle(title);
        item.setPrice(price);
        itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        Optional<Item> findItem = findItem(id);
        Item item = findItem.get();
        itemRepository.delete(item);
    }



}
