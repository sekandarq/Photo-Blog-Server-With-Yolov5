from django.urls import path, include
from blog import views

from rest_framework import routers

router = routers.DefaultRouter()
router.register('Post', views.BlogImageViewSet)


urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('post/<int:pk>/', views.post_detail, name='post_detail'),
    path('api_root/', include(router.urls)),    
]


