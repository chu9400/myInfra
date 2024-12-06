package com.hanul.myinfra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemService itemService;

    @GetMapping("/list")
    String showList(Model model) {
        List<Item> result = itemRepository.findAll();
        model.addAttribute("items", result);
        return "list.html";
    }

    @GetMapping("/write")
    String showWrite() {
        return "write.html";
    }

    @PostMapping("/add")
    String itemAdd(@RequestParam String title, @RequestParam Integer price) {
        itemService.saveItem(title, price);
        return "redirect:/list";
    }

    @GetMapping("/detail/{id}")
    String detail(@PathVariable Long id, Model model) {
        Optional<Item> findItem = itemService.findItem(id);

        if (findItem.isPresent()) {
            Item item = findItem.get();
            model.addAttribute("findItem", item);
            return "detail.html";
        } else {
            return "redirect:/list";
        }
    }

    @GetMapping("/edit/{id}")
    String edit(@PathVariable Long id, Model model) {
        Optional<Item> findItem = itemService.findItem(id);

        if (findItem.isPresent()) {
            Item item = findItem.get();
            model.addAttribute("findItem", item);
            return "edit.html";
        } else {
            return "redirect:/list";
        }
    }

    @PostMapping("/update")
    String itemUpdate(
            @RequestParam Long id,
            @RequestParam String title,
            @RequestParam Integer price
    )
    {
        itemService.updateItem(id, title, price);
        return "edit_success.html";
    }

    @PostMapping("/delete")
    String itemDelete(@RequestParam Long id) {
        itemService.deleteItem(id);
        return "delete_success.html";
    }

}