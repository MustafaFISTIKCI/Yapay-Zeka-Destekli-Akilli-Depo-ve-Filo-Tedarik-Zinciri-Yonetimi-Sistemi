from django.db import models

# Create your models here.
class Warehouse(models.Model):
    name = models.CharField(max_length=100, verbose_name="Depo Adı")
    location = models.CharField(max_length=255, verbose_name="Konum")
    capacity = models.IntegerField(verbose_name="Kapasite (m3)")

    def __str__(self):
        return self.name

class Product(models.Model):
    name = models.CharField(max_length=200, verbose_name="Ürün Adı")
    sku = models.CharField(max_length=50, unique=True, verbose_name="Stok Kodu")
    category = models.CharField(max_length=100, verbose_name="Kategori")
    stock_quantity = models.IntegerField(default=0, verbose_name="Stok Miktarı")
    warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE, related_name="products")

    def __str__(self):
        return self.name

class Vehicle(models.Model):
    plate_number = models.CharField(max_length=20, unique=True, verbose_name="Plaka")
    vehicle_type = models.CharField(max_length=50, verbose_name="Araç Tipi")
    is_available = models.BooleanField(default=True, verbose_name="Müsait mi?")

    def __str__(self):
        return self.plate_number