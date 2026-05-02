from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from .models import Warehouse, Product, Vehicle

@login_required
def dashboard(request):
    # Veritabanından özet bilgileri çekiyoruz
    context = {
        'total_warehouses': Warehouse.objects.count(),
        'total_products': Product.objects.count(),
        'total_vehicles': Vehicle.objects.count(),
        # Stoğu azalanları listele (örneğin 10 birimden az)
        'low_stock_products': Product.objects.filter(stock_quantity__lt=10),
        'user_name': request.user.username,
    }
    return render(request, 'logistics/dashboard.html', context)