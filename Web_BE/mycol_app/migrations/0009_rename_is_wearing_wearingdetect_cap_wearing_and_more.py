# Generated by Django 4.2.9 on 2024-01-14 05:44

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mycol_app', '0008_wearingdetect'),
    ]

    operations = [
        migrations.RenameField(
            model_name='wearingdetect',
            old_name='is_wearing',
            new_name='cap_wearing',
        ),
        migrations.AddField(
            model_name='wearingdetect',
            name='glasses_wearing',
            field=models.CharField(default='', max_length=50, null=True),
        ),
    ]
