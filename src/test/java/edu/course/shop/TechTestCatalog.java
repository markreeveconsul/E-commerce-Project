package edu.course.shop;

import edu.course.shop.model.*;
import edu.course.shop.service.*;

/** Explicit synthetic shopping fixtures; never run against the user's catalog. */
final class TechTestCatalog {
    private TechTestCatalog() { }
    static void prepare(ProductService products,User admin) {
        String[] names={"USB-C cable","USB flash drive","PC thermal paste","Case fan"};
        Category[] categories={Category.PERIPHERALS,Category.PERIPHERALS,Category.PC_PARTS,Category.PC_PARTS};
        for(int i=0;i<names.length;i++) {
            Product p=products.get(i+3);
            products.save(admin,p.getId(),names[i],categories[i],p.getPrice(),p.getStock(),true,"Synthetic shopping test fixture.",p.getPhoto(),p.getAdditionalPhotos(),p.getSpecifications(),p.getPhotoFrame());
        }
    }
}
